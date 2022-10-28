import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInputStream;

class mult_matrices {
    static int N = 4000;
    static float[][] A = new float[N][N];
    static float[][] B = new float[N][N];
    static float[][] C = new float[N][N];

    // Función que agrega una matriz cuadrada de tamaño [N/4][N/4] a una de tamaño
    // [N][N]
    static void agrega_a_matriz(float[][] matriz, int fila, int columna) {
        for (int i = fila; i < fila + (N / 4); i++) {
            for (int j = columna; j < columna + (N / 4); j++) {
                C[i][j] = matriz[i - fila][j - columna];
            }
        }
    }

    // Método para recibir una matriz cuadrada de tamaño [N/4][N/4]
    static float[][] recibe_matriz_cuadrada(DataInputStream entrada, int tam) throws Exception {

        float[][] matriz = new float[N / 4][N / 4];
        byte[] a = new byte[tam];
        read(entrada, a, 0, tam);
        ByteBuffer b = ByteBuffer.wrap(a);

        for (int i = 0; i < N / 4; i++) {
            for (int j = 0; j < N / 4; j++) {
                matriz[i][j] = b.getFloat();
            }
        }
        return matriz;
    }

    // Método estático para empaquetar una matriz cuadrada de tamaño [N/4][N/4]
    static byte[] empaqueta_matriz_cuadrada(float[][] matriz) throws IOException {
        ByteBuffer b = ByteBuffer.allocate((N * N) / 4);
        for (int i = 0; i < N / 4; i++) {
            for (int j = 0; j < N / 4; j++) {
                b.putFloat(matriz[i][j]);
            }
        }
        byte[] arreglo = b.array();
        return arreglo;
    }

    // Método estático para multiplicar matrices de tamaño [N/4][N]
    static float[][] obten_matriz(float[][] matriz1, float[][] matriz2) {

        // multiplica la matriz A y la matriz B, el resultado queda en la matriz C
        // notar que los indices de la matriz B se han intercambiado
        // matriz, es de tamaño [N/4][N/4], debido a que la matriz resultante tiene que
        // ser cuadrara
        // lo cual, también influye en los for's
        float[][] matriz = new float[N / 4][N / 4];
        for (int i = 0; i < N / 4; i++)
            for (int j = 0; j < N / 4; j++)
                for (int k = 0; k < N; k++)
                    matriz[i][j] += matriz1[i][k] * matriz2[j][k];
        return matriz;
    }

    // Método estático para recibir una matriz de [N/4][N]
    static float[][] recibir_matriz(DataInputStream entrada, int tam) throws Exception {
        float[][] matriz = new float[N / 4][N];
        byte[] a = new byte[tam];
        read(entrada, a, 0, tam);
        ByteBuffer b = ByteBuffer.wrap(a);
        for (int i = 0; i < N / 4; i++) {
            for (int j = 0; j < N; j++) {
                matriz[i][j] = b.getFloat();
            }
        }
        return matriz;
    }

    // Recibe los mismos 3 parámetros que el método read() de la clase
    // DataInputStream, pero recibe aparte como primer parámetro el flujo de entrada
    // de datos
    static void read(DataInputStream f, byte[] b, int posicion, int longitud) throws Exception {
        while (longitud > 0) {
            int n = f.read(b, posicion, longitud);
            posicion += n;
            longitud -= n;
        }
    }

    // Función que empaqueta una submatriz de una matriz principal en un arreglo de
    // bytes
    // Para ello le mandamos como parámetro desde qué fila queremos que empiece a
    // empaquetar (n)
    // A partir de esa fila, la función empaqueta 1/4 de la matriz
    static byte[] empaqueta_matriz(int n, String matriz) {
        ByteBuffer b = ByteBuffer.allocate(N * N);
        for (int i = n; i < n + (N / 4); i++) {
            for (int j = 0; j < N; j++) {
                if (matriz.equals("A")) {
                    b.putFloat(A[i][j]);
                } else {
                    b.putFloat(B[i][j]);
                }
            }
        }
        byte[] arreglo = b.array(); // Este método va a transformar nuestro buffer de bytes en un arreglo de bytes
        return arreglo;
    }

    static class Worker extends Thread {

        int nodo;

        Worker(int nodo) {
            this.nodo = nodo;
        }

        public void run() {
            if (nodo == 1) { // nodo 1
                try {
                    // Conexión con el nodo 1
                    Socket conexion = null;
                    for (;;) { // reintentos de conexión
                        try {
                            conexion = new Socket("40.85.91.218", 50000);
                            break; // sale del ciclo for en caso de que llegue a esta línea de código
                        } catch (Exception e) {
                            Thread.sleep(1000); // el programa se duerme 1 segundo
                        }
                    }
                    // Flujos de entrada y salida
                    DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                    DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());

                    // Enviar la matriz A1
                    byte[] A1 = empaqueta_matriz(0, "A");
                    salida.writeInt(A1.length);
                    salida.write(A1);

                    // Enviar la matriz A2
                    byte[] A2 = empaqueta_matriz(N / 4, "A");
                    salida.writeInt(A2.length);
                    salida.write(A2);

                    // Enviar la matriz B1
                    byte[] B1 = empaqueta_matriz(0, "B");
                    salida.writeInt(B1.length);
                    salida.write(B1);

                    // Enviar la matriz B2
                    byte[] B2 = empaqueta_matriz(N / 4, "B");
                    salida.writeInt(B2.length);
                    salida.write(B2);

                    // Enviar la matriz B3
                    byte[] B3 = empaqueta_matriz(N / 2, "B");
                    salida.writeInt(B3.length);
                    salida.write(B3);

                    // Enviar la matriz B4
                    byte[] B4 = empaqueta_matriz((3 * N) / 4, "B");
                    salida.writeInt(B4.length);
                    salida.write(B4);

                    // Recibir la matriz C1
                    int tam_matriz_C1 = entrada.readInt();
                    float[][] C1 = recibe_matriz_cuadrada(entrada, tam_matriz_C1);

                    // Recibir la matriz C2
                    int tam_matriz_C2 = entrada.readInt();
                    float[][] C2 = recibe_matriz_cuadrada(entrada, tam_matriz_C2);

                    // Recibir la matriz C3
                    int tam_matriz_C3 = entrada.readInt();
                    float[][] C3 = recibe_matriz_cuadrada(entrada, tam_matriz_C3);

                    // Recibir la matriz C4
                    int tam_matriz_C4 = entrada.readInt();
                    float[][] C4 = recibe_matriz_cuadrada(entrada, tam_matriz_C4);

                    // Recibir la matriz C5
                    int tam_matriz_C5 = entrada.readInt();
                    float[][] C5 = recibe_matriz_cuadrada(entrada, tam_matriz_C5);

                    // Recibir la matriz C6
                    int tam_matriz_C6 = entrada.readInt();
                    float[][] C6 = recibe_matriz_cuadrada(entrada, tam_matriz_C6);

                    // Recibir la matriz C7
                    int tam_matriz_C7 = entrada.readInt();
                    float[][] C7 = recibe_matriz_cuadrada(entrada, tam_matriz_C7);

                    // Recibir la matriz C8
                    int tam_matriz_C8 = entrada.readInt();
                    float[][] C8 = recibe_matriz_cuadrada(entrada, tam_matriz_C8);

                    // Añadir C1 a C
                    agrega_a_matriz(C1, 0, 0);

                    // Añadir C2 a C
                    agrega_a_matriz(C2, 0, N / 4);

                    // Añadir C3 a C
                    agrega_a_matriz(C3, 0, N / 2);

                    // Añadir C4 a C
                    agrega_a_matriz(C4, 0, (3 * N) / 4);

                    // Añadir C5 a C
                    agrega_a_matriz(C5, N / 4, 0);

                    // Añadir C6 a C
                    agrega_a_matriz(C6, N / 4, N / 4);

                    // Añadir C7 a C
                    agrega_a_matriz(C7, N / 4, N / 2);

                    // Añadir C8 a C
                    agrega_a_matriz(C8, N / 4, (3 * N) / 4);

                } catch (Exception e) {
                    System.out.println("Ha habido un error");
                }
            } else { // nodo 2
                try {
                    // Conexión con el nodo 2
                    Socket conexion = null;
                    for (;;) { // reintentos de conexión
                        try {
                            conexion = new Socket("52.237.203.91", 50000);
                            break; // sale del ciclo for en caso de que llegue a esta línea de código
                        } catch (Exception e) {
                            Thread.sleep(1000); // el programa se duerme 1 segundo
                        }
                    }
                    // Flujos de entrada y salida
                    DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                    DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());

                    // Enviar la matriz A3
                    byte[] A3 = empaqueta_matriz(N / 2, "A");
                    salida.writeInt(A3.length);
                    salida.write(A3);

                    // Enviar la matriz A4
                    byte[] A4 = empaqueta_matriz((3 * N) / 4, "A");
                    salida.writeInt(A4.length);
                    salida.write(A4);

                    // Enviar la matriz B1
                    byte[] B1 = empaqueta_matriz(0, "B");
                    salida.writeInt(B1.length);
                    salida.write(B1);

                    // Enviar la matriz B2
                    byte[] B2 = empaqueta_matriz(N / 4, "B");
                    salida.writeInt(B2.length);
                    salida.write(B2);

                    // Enviar la matriz B3
                    byte[] B3 = empaqueta_matriz(N / 2, "B");
                    salida.writeInt(B3.length);
                    salida.write(B3);

                    // Enviar la matriz B4
                    byte[] B4 = empaqueta_matriz((3 * N) / 4, "B");
                    salida.writeInt(B4.length);
                    salida.write(B4);

                    // Recibir la matriz C9
                    int tam_matriz_C9 = entrada.readInt();
                    float[][] C9 = recibe_matriz_cuadrada(entrada, tam_matriz_C9);

                    // Recibir la matriz C10
                    int tam_matriz_C10 = entrada.readInt();
                    float[][] C10 = recibe_matriz_cuadrada(entrada, tam_matriz_C10);

                    // Recibir la matriz C11
                    int tam_matriz_C11 = entrada.readInt();
                    float[][] C11 = recibe_matriz_cuadrada(entrada, tam_matriz_C11);

                    // Recibir la matriz C12
                    int tam_matriz_C12 = entrada.readInt();
                    float[][] C12 = recibe_matriz_cuadrada(entrada, tam_matriz_C12);

                    // Recibir la matriz C13
                    int tam_matriz_C13 = entrada.readInt();
                    float[][] C13 = recibe_matriz_cuadrada(entrada, tam_matriz_C13);

                    // Recibir la matriz C14
                    int tam_matriz_C14 = entrada.readInt();
                    float[][] C14 = recibe_matriz_cuadrada(entrada, tam_matriz_C14);

                    // Recibir la matriz C15
                    int tam_matriz_C15 = entrada.readInt();
                    float[][] C15 = recibe_matriz_cuadrada(entrada, tam_matriz_C15);

                    // Recibir la matriz C16
                    int tam_matriz_C16 = entrada.readInt();
                    float[][] C16 = recibe_matriz_cuadrada(entrada, tam_matriz_C16);

                    // Añadir C9 a C
                    agrega_a_matriz(C9, N / 2, 0);

                    // Añadir C10 a C
                    agrega_a_matriz(C10, N / 2, N / 4);

                    // Añadir C11 a C
                    agrega_a_matriz(C11, N / 2, N / 2);

                    // Añadir C12 a C
                    agrega_a_matriz(C12, N / 2, (3 * N) / 4);

                    // Añadir C13 a C
                    agrega_a_matriz(C13, (3 * N) / 4, 0);

                    // Añadir C14 a C
                    agrega_a_matriz(C14, (3 * N) / 4, N / 4);

                    // Añadir C15 a C
                    agrega_a_matriz(C15, (3 * N) / 4, N / 2);

                    // Añadir C16 a C
                    agrega_a_matriz(C16, (3 * N) / 4, (3 * N) / 4);

                } catch (Exception e) {
                    System.out.println("Ha habido un error");
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

        int opcion = Integer.parseInt(args[0]);
        switch (opcion) {
            case 0:
                // inicializa las matrices A y B

                for (int i = 0; i < N; i++)
                    for (int j = 0; j < N; j++) {
                        A[i][j] = i + 3 * j;
                        B[i][j] = 2 * i - j;
                        C[i][j] = 0;
                    }

                // transpone la matriz B, la matriz traspuesta queda en B

                for (int i = 0; i < N; i++)
                    for (int j = 0; j < i; j++) {
                        float x = B[i][j];
                        B[i][j] = B[j][i];
                        B[j][i] = x;
                    }

                // Hilos
                Worker nodo1 = new Worker(1);
                Worker nodo2 = new Worker(2);
                nodo1.start();
                nodo2.start();
                nodo1.join();
                nodo2.join();

                // Calculamos el checksum
                float checksum = 0;
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        checksum = checksum + C[i][j];
                    }
                }

                if (N == 12) {

                    System.out.println("Como N = 12, entonces imprimiremos las matrices");
                    System.out.println("Las matrices a multiplicar son:");
                    System.out.println("Matriz A:");

                    for (int i = 0; i < N; i++) {
                        for (int j = 0; j < N; j++) {
                            System.out.print(" " + A[i][j]);
                        }
                        System.out.println("");
                    }

                    System.out.println("Matriz B:");
                    for (int i = 0; i < N; i++) {
                        for (int j = 0; j < N; j++) {
                            System.out.print(" " + B[i][j]);
                        }
                        System.out.println("");
                    }

                    System.out.println("Matriz resultante:");
                    for (int i = 0; i < N; i++) {
                        for (int j = 0; j < N; j++) {
                            System.out.print(" " + C[i][j]);
                        }
                        System.out.println("");
                    }

                    System.out.println("El checksum para la matriz resultante es: " + checksum);

                } else {
                    System.out.println("Como N = 4000, imprimiremos solamente el checksum");
                    System.out.println("");
                    System.out.println("checksum = " + checksum);
                }

                break;

            case 1:
                // Variables para el servidor
                ServerSocket servidor = new ServerSocket(50000);
                Socket conexion = servidor.accept();

                // Flujo de entrada y salida
                DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());

                // Recibir la matriz A1
                int A1_tam = entrada.readInt();
                float[][] A1 = recibir_matriz(entrada, A1_tam);

                // Recibir la matriz A2
                int A2_tam = entrada.readInt();
                float[][] A2 = recibir_matriz(entrada, A2_tam);

                // Recibir la matriz B1
                int B1_tam = entrada.readInt();
                float[][] B1 = recibir_matriz(entrada, B1_tam);

                // Recibir la matriz B2
                int B2_tam = entrada.readInt();
                float[][] B2 = recibir_matriz(entrada, B2_tam);

                // Recibir la matriz B3
                int B3_tam = entrada.readInt();
                float[][] B3 = recibir_matriz(entrada, B3_tam);

                // Recibir la matriz B4
                int B4_tam = entrada.readInt();
                float[][] B4 = recibir_matriz(entrada, B4_tam);

                // Obtener C1
                float[][] C1 = obten_matriz(A1, B1);

                // Obtener C2
                float[][] C2 = obten_matriz(A1, B2);

                // Obtener C3
                float[][] C3 = obten_matriz(A1, B3);

                // Obtener C4
                float[][] C4 = obten_matriz(A1, B4);

                // Obtener C5
                float[][] C5 = obten_matriz(A2, B1);

                // Obtener C6
                float[][] C6 = obten_matriz(A2, B2);

                // Obtener C7
                float[][] C7 = obten_matriz(A2, B3);

                // Obtener C8
                float[][] C8 = obten_matriz(A2, B4);

                // Enviar la matriz C1
                byte[] arregloC1 = empaqueta_matriz_cuadrada(C1);
                salida.writeInt(arregloC1.length);
                salida.write(arregloC1);

                // Enviar la matriz C2
                byte[] arregloC2 = empaqueta_matriz_cuadrada(C2);
                salida.writeInt(arregloC2.length);
                salida.write(arregloC2);

                // Enviar la matriz C3
                byte[] arregloC3 = empaqueta_matriz_cuadrada(C3);
                salida.writeInt(arregloC3.length);
                salida.write(arregloC3);

                // Enviar la matriz C4
                byte[] arregloC4 = empaqueta_matriz_cuadrada(C4);
                salida.writeInt(arregloC4.length);
                salida.write(arregloC4);

                // Enviar la matriz C5
                byte[] arregloC5 = empaqueta_matriz_cuadrada(C5);
                salida.writeInt(arregloC5.length);
                salida.write(arregloC5);

                // Enviar la matriz C6
                byte[] arregloC6 = empaqueta_matriz_cuadrada(C6);
                salida.writeInt(arregloC6.length);
                salida.write(arregloC6);

                // Enviar la matriz C7
                byte[] arregloC7 = empaqueta_matriz_cuadrada(C7);
                salida.writeInt(arregloC7.length);
                salida.write(arregloC7);

                // Enviar la matriz C8
                byte[] arregloC8 = empaqueta_matriz_cuadrada(C8);
                salida.writeInt(arregloC8.length);
                salida.write(arregloC8);

                break;
            case 2:
                // Variables para el servidor
                ServerSocket server = new ServerSocket(50000);
                Socket enlace = server.accept();

                // Flujo de entrada y salida
                DataInputStream input = new DataInputStream(enlace.getInputStream());
                DataOutputStream output = new DataOutputStream(enlace.getOutputStream());

                // Recibir la matriz A3
                int A3_tam = input.readInt();
                float[][] A3 = recibir_matriz(input, A3_tam);

                // Recibir la matriz A4
                int A4_tam = input.readInt();
                float[][] A4 = recibir_matriz(input, A4_tam);

                // Recibir la matriz B1
                int b1_tam = input.readInt();
                float[][] b1 = recibir_matriz(input, b1_tam);

                // Recibir la matriz B2
                int b2_tam = input.readInt();
                float[][] b2 = recibir_matriz(input, b2_tam);

                // Recibir la matriz B3
                int b3_tam = input.readInt();
                float[][] b3 = recibir_matriz(input, b3_tam);

                // Recibir la matriz B4
                int b4_tam = input.readInt();
                float[][] b4 = recibir_matriz(input, b4_tam);

                // Obtener C9
                float[][] C9 = obten_matriz(A3, b1);

                // Obtener C10
                float[][] C10 = obten_matriz(A3, b2);

                // Obtener C11
                float[][] C11 = obten_matriz(A3, b3);

                // Obtener C12
                float[][] C12 = obten_matriz(A3, b4);

                // Obtener C13
                float[][] C13 = obten_matriz(A4, b1);

                // Obtener C14
                float[][] C14 = obten_matriz(A4, b2);

                // Obtener C15
                float[][] C15 = obten_matriz(A4, b3);

                // Obtener C16
                float[][] C16 = obten_matriz(A4, b4);

                // Enviar la matriz C9
                byte[] arregloC9 = empaqueta_matriz_cuadrada(C9);
                output.writeInt(arregloC9.length);
                output.write(arregloC9);

                // Enviar la matriz C10
                byte[] arregloC10 = empaqueta_matriz_cuadrada(C10);
                output.writeInt(arregloC10.length);
                output.write(arregloC10);

                // Enviar la matriz C11
                byte[] arregloC11 = empaqueta_matriz_cuadrada(C11);
                output.writeInt(arregloC11.length);
                output.write(arregloC11);

                // Enviar la matriz C12
                byte[] arregloC12 = empaqueta_matriz_cuadrada(C12);
                output.writeInt(arregloC12.length);
                output.write(arregloC12);

                // Enviar la matriz C13
                byte[] arregloC13 = empaqueta_matriz_cuadrada(C13);
                output.writeInt(arregloC13.length);
                output.write(arregloC13);

                // Enviar la matriz C14
                byte[] arregloC14 = empaqueta_matriz_cuadrada(C14);
                output.writeInt(arregloC14.length);
                output.write(arregloC14);

                // Enviar la matriz C15
                byte[] arregloC15 = empaqueta_matriz_cuadrada(C15);
                output.writeInt(arregloC15.length);
                output.write(arregloC15);

                // Enviar la matriz C16
                byte[] arregloC16 = empaqueta_matriz_cuadrada(C16);
                output.writeInt(arregloC16.length);
                output.write(arregloC16);

                break;
            default:

                break;
        }

    }
}