package texture

import com.kgl.glfw.*
import com.kgl.opengl.*
import com.kgl.stb.Channels
import com.kgl.stb.STBIOCallbacks
import com.kgl.stb.STBImage
import kotlinx.cinterop.*
import kotlinx.io.core.IoBuffer
import kotlinx.io.streams.fread
import platform.posix.*

// language=glsl
const val vertexShaderSource = """
#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec2 aTexCoord;

out vec3 ourColor;
out vec2 TexCoord;

void main()
{
    gl_Position = vec4(aPos, 1.0);
    ourColor = aColor;
    TexCoord = aTexCoord;
}
"""

// language=glsl
const val fragmentShaderSource = """
#version 330 core
out vec4 FragColor;
  
in vec3 ourColor;
in vec2 TexCoord;

uniform sampler2D ourTexture;

void main()
{
    FragColor = texture(ourTexture, TexCoord);
}
"""

inline val Float.Companion.SIZE_BYTES get() = Int.SIZE_BYTES

fun main() {
    Glfw.init()
    val window = Window(800, 600, "Texture") {
        contextVersionMajor = 3
        contextVersionMinor = 3
        openGLProfile = OpenGLProfile.Core
        if (Platform.osFamily == OsFamily.MACOSX) {
            openGLForwardCompat = true
        }
    }
    Glfw.currentContext = window

    window.setFrameBufferCallback { _, width, height ->
        glViewport(0, 0, width, height)
    }

    val vertices = floatArrayOf(
        // positions          // colors           // texture coords
         0.5f,  0.5f, 0.0f,   1.0f, 0.0f, 0.0f,   1.0f, 1.0f, // top right
         0.5f, -0.5f, 0.0f,   0.0f, 1.0f, 0.0f,   1.0f, 0.0f, // bottom right
        -0.5f, -0.5f, 0.0f,   0.0f, 0.0f, 1.0f,   0.0f, 0.0f, // bottom left
        -0.5f,  0.5f, 0.0f,   1.0f, 1.0f, 0.0f,   0.0f, 1.0f  // top left
    )
    val indices = intArrayOf(
        0, 1, 3, // first triangle
        1, 2, 3  // second triangle
    )

    val vao = glGenVertexArray()
    val vbo = glGenBuffer()
    val ebo = glGenBuffer()

    glBindVertexArray(vao)

    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, (vertices.size * Float.SIZE_BYTES).convert(), vertices.refTo(0), GL_STATIC_DRAW)

    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, (indices.size * UInt.SIZE_BYTES).convert(), indices.refTo(0), GL_STATIC_DRAW)

    // position attribute
    glVertexAttribPointer(0u, 3, GL_FLOAT, false, 8 * Float.SIZE_BYTES, null)
    glEnableVertexAttribArray(0u)
    // color attribute
    glVertexAttribPointer(1u, 3, GL_FLOAT, false, 8 * Float.SIZE_BYTES, (3L * Float.SIZE_BYTES).toCPointer<CPointed>())
    glEnableVertexAttribArray(1u)
    // texture coord attribute
    glVertexAttribPointer(2u, 2, GL_FLOAT, false, 8 * Float.SIZE_BYTES, (6L * Float.SIZE_BYTES).toCPointer<CPointed>())
    glEnableVertexAttribArray(2u)

    val texture1 = loadTexture("./textures/container.jpg")

    val ourShader: UInt = run {
        val vertexShaderId = glCreateShader(GL_VERTEX_SHADER)
        val fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER)

        glShaderSource(vertexShaderId, vertexShaderSource)
        glCompileShader(vertexShaderId)

        glGetShaderInfoLog(vertexShaderId).also {
            if (it.isNotBlank()) println()
        }

        glShaderSource(fragmentShaderId, fragmentShaderSource)
        glCompileShader(fragmentShaderId)

        glGetShaderInfoLog(fragmentShaderId).also {
            if (it.isNotBlank()) println()
        }

        val programId = glCreateProgram()
        glAttachShader(programId, vertexShaderId)
        glAttachShader(programId, fragmentShaderId)
        glLinkProgram(programId)

        glGetProgramInfoLog(programId).also {
            if (it.isNotBlank()) println()
        }

        glDetachShader(programId, vertexShaderId)
        glDetachShader(programId, fragmentShaderId)

        glDeleteShader(vertexShaderId)
        glDeleteShader(fragmentShaderId)

        programId
    }

    glUseProgram(ourShader)
    glUniform1i(glGetUniformLocation(ourShader, "ourTexture"), 0)

    while (!window.shouldClose) {
        if (window.getKey(KeyboardKey.ESCAPE) == Action.Press) {
            window.shouldClose = true
        }

        glClearColor(0.2f, 0.3f, 0.3f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT)

        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, texture1)

        glUseProgram(ourShader)
        glBindVertexArray(vao)
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, null)

        window.swapBuffers()
        Glfw.pollEvents()
    }

    glDeleteVertexArrays(1, cValuesOf(vao))
    glDeleteBuffers(1, cValuesOf(vbo))
    glDeleteBuffers(1, cValuesOf(ebo))

    window.close()
    Glfw.terminate()
}

fun loadTexture(path: String): UInt {
    val texture = glGenTexture()
    glBindTexture(GL_TEXTURE_2D, texture)

    // set the texture wrapping parameters
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT.toInt())	// set texture wrapping to GL_REPEAT (default wrapping method)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT.toInt())
    // set texture filtering parameters
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR.toInt())
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR.toInt())
    // load image, create texture and generate mipmaps
    STBImage.setFlipVerticallyOnLoad(true) // tell stb_image.h to flip loaded texture's on the y-axis.

    val file = fopen(path, "rb")!!
    val image = try {
        STBImage.load(object : STBIOCallbacks {
            override fun read(data: IoBuffer): Int {
                return fread(data, file).toInt()
            }

            override fun skip(n: Int) {
                fseek(file, n.toLong(), SEEK_CUR)
            }

            override val eof: Boolean get() = feof(file) != 0
        }, Channels.RGB_ALPHA)
    } finally {
        fclose(file)
    }

    image.buffer.readDirect {
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA.toInt(), image.info.width, image.info.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, it)
        0
    }
    glGenerateMipmap(GL_TEXTURE_2D)
    image.close()
    return texture
}
