import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Ejercicio2 {

	private FTPClient client;
	private final List<String> usersLog = new ArrayList<>();

	public void proceso() throws IOException {
		try {
//			BufferedReader entradaDatos = new BufferedReader(new InputStreamReader(System.in));

			String username;
			Console console = System.console();
			do {
				if (console == null) {
					throw new NullPointerException("Fallo al escribir por consola");
				}
				username = console.readLine("Introduce Nombre de usuario: ");
//				System.out.print("Nombre de usuario: ");
//				username = entradaDatos.readLine();
				if (username.isEmpty()) {
					throw new IllegalArgumentException("El campo no puede estar vacío");
				}
				if (!username.trim().equals("*")) {
					char[] passwordArray = console.readPassword("Contraseña: ");
					if (passwordArray == null) {
						throw new IllegalArgumentException("El campo no puede ser nulo");
					}
//					System.out.print("Contraseña: ");
					String password = new String(passwordArray);
//					String password = entradaDatos.readLine();
					writeLog(username, password);
				}

			} while (!username.equals("*"));
			System.out.println("=============================================");
			System.out.println("DATOS DE LA CUENTA DE GMAIL");

			String email = "";
			boolean isValid;
			int tries = 0;
			do {
				try {
					System.out.print("Cuenta de usuario: ");
					email = console.readLine("Cuenta de usuario: ");
//					email = entradaDatos.readLine();
					isValid = checkEmail(email);
					tries++;

				} catch (Exception e) {
					e.getLocalizedMessage();
					isValid = false;
				}
			} while (!isValid && tries < 2);
			StringBuilder body = new StringBuilder();

//			System.out.print("Introduce la clave: ");
//			String password = entradaDatos.readLine();
//			if (password == null || password.isEmpty()) {
//				System.err.println("La contraseña introducida no es válida");
//			}
			char[] passwordArray = console.readPassword("Introduce la clave: ");
			if (passwordArray == null) {
				throw new IllegalArgumentException("El campo no puede ser nulo");
			}
			String password = new String(passwordArray);

			body.append("Conexiones realizadas durante la ejecución del Ejercicio 2: ")
				.append(this.usersLog.size())
				.append("\nSon las siguientes:\n");
			for (String user : usersLog) {
				body.append("\t")
					.append(user)
					.append("\n");
			}
			sendEmail(email, email, password, body.toString());
			System.out.println("Enviado!");
		} catch (Exception e) {
			System.err.println(e.getLocalizedMessage());
		} finally {
			this.client.disconnect();
		}
	}

	private boolean checkEmail(String email) {
		if (email == null || email.isEmpty()) {
			System.out.println("El email no puede estar vacío. Introdúcelo de nuevo");
			return false;
		}
		if (!email.contains("@")) {
			System.out.println("El email introducido no es válido. Introdúcelo de nuevo");
			return false;
		}
		return true;
	}

	private void writeLog(String user, String password) throws IOException {
		if (!login(user, password)) {
			System.out.println("\t\tLogin incorrecto...");
			return;
		}
		System.out.println("\t\tNos conectamos a: localhost");
		//Nos quedamos con el número
		int userId;
		try {
			userId = Integer.parseInt(user.replaceAll("\\D+", ""));
		} catch (Exception e) {
			System.out.println("Ha ocurrido un error al obtener el identificador del usuario...");
			return;
		}
		usersLog.add(user);
		String fileName = "LOG.txt";
		String systemDrive = System.getProperty("user.dir");
		String directoryName = systemDrive + File.separator + user;
		String pathname = directoryName + File.separator + fileName;
		File directory = new File(directoryName);
		if (!directory.exists()) {
			if (!directory.mkdir()) {
				System.err.println("Ha ocurrido un error creando el directorio del usuario");
			}
		}
		File file = new File(pathname);
		if (!file.exists()) {
			if (!file.createNewFile()) {
				System.err.println("Ha ocurrido un error creando el archivo LOG.txt");
			}
		}
		FileOutputStream out = new FileOutputStream(file);
		client.changeWorkingDirectory("LOG");
		FTPFile[] ftpFiles = client.listFiles();
		if (ftpFiles.length <= 0) {
			out.write(String.format("Conexiones realizadas por el Usuario %d.\n", userId).getBytes());
			out.flush();
		} else {
			client.retrieveFile("LOG.txt", out);
		}
		String date = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss zzz uuuu")) + "\n";
		out.write(date.getBytes());
		out.flush();
		String remoteFile = String.format("%s/%s", client.printWorkingDirectory(), fileName);
		InputStream inputStream = new FileInputStream(file);
		client.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
		client.enterLocalPassiveMode();
		out.close();
		boolean done = client.storeFile(remoteFile, inputStream);
		System.out.print(client.getReplyString());
		inputStream.close();
		if (!done) {
			System.out.println("\tHa ocurrido un error al actualizar el LOG.txt");
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

	private void sendEmail(String from, String to, String password, String body) throws
																				 MessagingException {

		Properties props = new Properties();
		// Nombre del host de correo, es smtp.gmail.com
		props.setProperty("mail.smtp.host", "smtp.gmail.com");
		// TLS si está disponible
		props.setProperty("mail.smtp.starttls.enable", "true");
		// Puerto de gmail para envio de correos
		props.setProperty("mail.smtp.port", "587");
		// Nombre del usuario
		props.setProperty("mail.smtp.user", from);
		// Si requiere o no usuario y password para conectarse.
		props.setProperty("mail.smtp.auth", "true");

		Session session = Session.getInstance(props,
											  new javax.mail.Authenticator() {
												  protected PasswordAuthentication getPasswordAuthentication() {
													  return new PasswordAuthentication(from, password);
												  }
											  });
		// Para obtener un log de salida más extenso
		session.setDebug(false);
		MimeMessage message = new MimeMessage(session);
		// Quien envia el correo
		message.setFrom(new InternetAddress(from));
		// A quien va dirigido
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		message.setSubject("RESULTADO Tarea5 Ortiz Sobrino Elena");
		message.setText(body);

		Transport.send(message);
	}
}

