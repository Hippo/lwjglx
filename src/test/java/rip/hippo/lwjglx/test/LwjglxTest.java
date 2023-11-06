package rip.hippo.lwjglx.test;


import static org.lwjgl.opengl.GL32.*;

import org.lwjglx.LWJGLException;
import org.lwjglx.opengl.Display;
import org.lwjglx.opengl.DisplayMode;

public final class LwjglxTest {
    
    private static final String VERTEX_SHADER = """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            void main() {
                gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
            }
            """;

    private static final String FRAGMENT_SHADER = """
            #version 330 core
            out vec4 FragColor;
            void main() {
                FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
            }
            """;

    public static void main(String[] args) {
        try {
            Display.setDisplayMode(new DisplayMode(800, 600));
            Display.setTitle("Lwjglx Test");
            Display.create();
        } catch (LWJGLException e) {
            throw new RuntimeException(e);
        }

        System.out.println(glGetString(GL_VERSION));


        float[] vertices = {
           -0.5f, -0.5f, 0.0f,
           0.5f, -0.5f, 0.0f,
           0.0f,  0.5f, 0.0f
        };

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, VERTEX_SHADER);
        glCompileShader(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, FRAGMENT_SHADER);
        glCompileShader(fragmentShader);

        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        glUseProgram(shaderProgram);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);


        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * 4, 0);
        glEnableVertexAttribArray(0);


        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);


        while (!Display.isCloseRequested()) {
            glClear(GL_COLOR_BUFFER_BIT);

            glUseProgram(shaderProgram);
            glBindVertexArray(vao);
            glDrawArrays(GL_TRIANGLES, 0, 3);

            Display.update();
        }

        Display.destroy();
    }
}
