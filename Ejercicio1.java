import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class Ejercicio1 {

	private FTPClient client;

	public void proceso() {
		String username;
		Console console = System.console();
		do {
			if (console == null) {
				throw new NullPointerException("Fallo al escribir por consola");
			}
			username = console.readLine("Nombre de usuario: ");
			if (username.isEmpty()) {
				throw new IllegalArgumentException("El campo no puede estar vacío");
			}if(!username.trim().equals("*")){
				char[] passwordArray = console.readPassword("Contraseña: ");
				if (passwordArray == null) {
					throw new IllegalArgumentException("El campo no puede ser nulo");
				}
				String password = new String(passwordArray);
				subirArchivoFTP(username, password);
			}

		} while (!username.equals("*"));
	}

	private void subirArchivoFTP(String username, String password) {
		try {

			System.out.println("Conectándose a 127.0.0.1");
			if (!login(username, password)) {
				System.out.println("\t\tUSUARIO Y/O CLAVE INCORRECTOS...");
				return;
			}
			File selectedFile;
			int tries = 0;
			final int MAX_TRIES = 2;
			//Le pedimos que seleccione un archivo válido hasta 3 veces
			do {
				JFileChooser jFileChooser = new JFileChooser() {
					@Override
					protected JDialog createDialog(Component parent) throws HeadlessException {
						JDialog dialog = super.createDialog(parent);
						//Lo ponemos delante de todas las ventanas
						dialog.setAlwaysOnTop(true);
						return dialog;
					}
				};
				jFileChooser.showSaveDialog(null);
				selectedFile = jFileChooser.getSelectedFile();
				if (selectedFile == null) {
					JOptionPane.showMessageDialog(jFileChooser,
												  "El archivo seleccionado no puede ser nulo");
				}
				tries++;
			} while (selectedFile == null && tries < MAX_TRIES);
			//Si no ha seleccionado uno válido nos salimos
			if (selectedFile == null) {
				JOptionPane.showMessageDialog(null,
											  "No se ha escogido ningún archivo para subir. Cerrando sesión...");
				return;

			}

			String remoteFile = String.format("%s/%s", client.printWorkingDirectory(),
											  selectedFile.getName());
			InputStream inputStream = new FileInputStream(selectedFile);

			boolean done = client.storeFile(remoteFile, inputStream);
			inputStream.close();

			if (!done) {
				JOptionPane.showMessageDialog(null,
											  String.format("Ha ocurrido un error al subir el archivo -> %s",
															selectedFile.getName()));
				return;
			}

			System.out.println("\t\tFICHERO SUBIDO CORRECTAMENTE");
			JOptionPane.showMessageDialog(null,
										  String.format("%s -> Se ha subido correctamente",
														selectedFile.getName()));
			FTPFile[] ftpFiles = client.listFiles();
			System.out.printf("\t\t\tFicheros en el directorio actual: %s\n", ftpFiles.length);
			for (FTPFile ftpFile : ftpFiles) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("\t\t\t")
							 .append(ftpFile.getName())
							 .append(" * ");
				if (ftpFile.isDirectory()) {
					stringBuilder.append("Directorio");
				} else if (ftpFile.isFile()) {
					stringBuilder.append("Fichero");
				}
				System.out.println(stringBuilder);
			}

		} catch (Exception e) {
			System.out.printf("Ha ocurrido error. Error: %s. Inténtelo de nuevo",
							  e.getLocalizedMessage());
		}
	}

	private boolean login(String user, String password) {
		this.client = new FTPClient();

		try {
			client.connect("localhost");
			return client.login(user, password);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

}
