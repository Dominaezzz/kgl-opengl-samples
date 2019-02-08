package triangle

import com.kgl.glfw.Glfw
import com.kgl.glfw.OpenGLProfile
import com.kgl.glfw.Window
import com.kgl.glfw.pollEvents
import com.kgl.opengl.*
import kotlinx.cinterop.*

// language=glsl
const val vertexShaderSource = """
#version 330 core

layout(location = 0) in vec3 vertexPosition_modelspace;

void main() {
    gl_Position.xyz = vertexPosition_modelspace;
    gl_Position.w = 1.0;
}
"""

// language=glsl
const val fragmentShaderSource = """
#version 330 core

out vec3 color;

void main(){
  color = vec3(1,0,0);
}
"""


fun main() {
    Glfw.init()

    val window = Window(512, 384, "Tutorial 01", null, null) {
        samples = 4
        contextVersionMajor = 3
        contextVersionMinor = 3
        openGLForwardCompat = true
        openGLProfile = OpenGLProfile.Core

        resizable = false
    }
    Glfw.currentContext = window

    println(glGetString(GL_VERSION)!!.reinterpret<ByteVar>().toKString())

    val vertexArrayID = memScoped {
        val output = alloc<UIntVar>()
        glGenVertexArrays(1, output.ptr)
        output.value
    }
    glBindVertexArray(vertexArrayID)

    val vertexBufferData = floatArrayOf(
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        0.0f,  1.0f, 0.0f
    )

    val vertexBuffer = memScoped {
        val output = alloc<UIntVar>()
        glGenBuffers(1, output.ptr)
        output.value
    }
    glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)

    vertexBufferData.usePinned {
        glBufferData(GL_ARRAY_BUFFER, vertexBufferData.size.toLong() * 4, it.addressOf(0), GL_STATIC_DRAW)
    }

    val programId = memScoped {
        val vertexShaderId = glCreateShader(GL_VERTEX_SHADER)
        val fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER)

        glShaderSource(vertexShaderId, 1, arrayOf(vertexShaderSource).toCStringArray(memScope), null)
        glCompileShader(vertexShaderId)

        glGetShaderInfoLog(vertexShaderId).also {
            if (it.isNotBlank()) println()
        }

        glShaderSource(fragmentShaderId, 1, arrayOf(fragmentShaderSource).toCStringArray(memScope), null)
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

    do {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glClearColor(0.1f, 0.2f, 0.3f, 1.0f)

        glUseProgram(programId)

        glEnableVertexAttribArray(0U)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)
        glVertexAttribPointer(
            0U,
            3,
            GL_FLOAT,
            false.toByte().toUByte(),
            0,
            0L.toCPointer<CPointed>()
        )

        glDrawArrays(GL_TRIANGLES, 0, 3)
        glDisableVertexAttribArray(0U)

        window.swapBuffers()
        pollEvents()
    } while (!window.shouldClose)

    window.close()
    Glfw.terminate()
}
