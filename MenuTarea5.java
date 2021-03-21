import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MenuTarea5 {

	private void getMenu() throws IOException, MessagingException {
		int opcionSeleccionada = 0;
		do {
			BufferedReader entradaDatos = new BufferedReader(new InputStreamReader(System.in));
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("================================\n")
						 .append("1 - EJERCICIO 1 - SUBIR FICHEROS\n")
						 .append("2 - EJERCICIO 2 - CONTROL LOG\n")
						 .append("3 - SALIR DEL PROGRAMA\n")
						 .append("================================\n")
						 .append("Introduce la opción del menú: ");

			String entradaUsuario;
			boolean isValid;
			do {
				System.out.print(stringBuilder.toString());
				try {
					do {
						entradaUsuario = entradaDatos.readLine();
						if (entradaUsuario.isEmpty()) {
							System.out.println("Debe introducir una opción del menú");
						} else {
							entradaUsuario = entradaUsuario.trim();
						}
					} while (entradaUsuario.isEmpty());

					opcionSeleccionada = Integer.parseInt(entradaUsuario);
					isValid = true;
				} catch (Exception e) {
					isValid = false;
					System.out.printf("Ha introducido \"%s\". Introduzca una opción válida\n",
									  opcionSeleccionada);
				}
			} while (!isValid);
			switch (opcionSeleccionada) {
				case 1:
					System.out.println("SUBIR FICHEROS AL SERVIDOR");
					System.out.println("=============================================");
					Ejercicio1 ejercicio1 = new Ejercicio1();
					ejercicio1.proceso();
					break;
				case 2:
					System.out.println("EJERCICIO 2 - CONTROL DE LOG Y ENVÍO DE CORREO");
					System.out.println("=============================================");
					Ejercicio2 ejercicio2 = new Ejercicio2();
					ejercicio2.proceso();
					break;
				case 3:
					System.out.println("Ha escogido cerrar el programa");
					break;
				default:
					System.out.println("La opción seleccionada no es válida");
			}
		} while (opcionSeleccionada != 3);

	}

	public static void main(String[] args) throws IOException, MessagingException {
		MenuTarea5 menuTarea5 = new MenuTarea5();
		menuTarea5.getMenu();
	}
}
