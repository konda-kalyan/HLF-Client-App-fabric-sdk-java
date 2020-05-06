package com.example.hlfclient;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {
	
	public static final byte[] EXPECTED_EVENT_DATA = "!".getBytes(UTF_8);
	public static final String EXPECTED_EVENT_NAME = "event";

	public static void writeUserContext(UserContext userContext) throws Exception {
		String directoryPath = "users/" + userContext.getAffiliation();
		String filePath = directoryPath + "/" + userContext.getName() + ".ser";
		File directory = new File(directoryPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}

		FileOutputStream file = new FileOutputStream(filePath);
		ObjectOutputStream out = new ObjectOutputStream(file);
		out.writeObject(userContext);
		out.close();
		file.close();
	}

	public static UserContext readUserContext(String affiliation, String username) throws Exception {
		String filePath = "users/" + affiliation + "/" + username + ".ser";
		File file = new File(filePath);
		if (file.exists()) {
			FileInputStream fileStream = new FileInputStream(filePath);
			ObjectInputStream in = new ObjectInputStream(fileStream);
			UserContext uContext = (UserContext) in.readObject();
			in.close();
			fileStream.close();
			return uContext;
		} else {
			return null;
		}
	}

	/*
	public static CAEnrollment getEnrollment(String keyFolderPath, String keyFileName, String certFolderPath,
			String certFileName)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CryptoException {
		PrivateKey key = null;
		String certificate = null;
		InputStream isKey = null;
		BufferedReader brKey = null;

		try {
			isKey = new FileInputStream(keyFolderPath + File.separator + keyFileName);
			brKey = new BufferedReader(new InputStreamReader(isKey));
			StringBuilder keyBuilder = new StringBuilder();

			for (String line = brKey.readLine(); line != null; line = brKey.readLine()) {
				if (line.indexOf("PRIVATE") == -1) {
					keyBuilder.append(line);
				}
			}

			certificate = new String(Files.readAllBytes(Paths.get(certFolderPath, certFileName)));
			byte[] encoded = DatatypeConverter.parseBase64Binary(keyBuilder.toString());
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
			KeyFactory kf = KeyFactory.getInstance("EC");
			key = kf.generatePrivate(keySpec);
		} finally {
			isKey.close();
			brKey.close();
		}

		CAEnrollment var15 = new CAEnrollment(key, certificate);
		return var15;
	}
	*/

	public static void cleanUp() {
		String directoryPath = "users";
		File directory = new File(directoryPath);
		deleteDirectory(directory);
	}

	public static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();

			for (int i = 0; i < children.length; ++i) {
				boolean success = deleteDirectory(children[i]);
				if (!success) {
					return false;
				}
			}
		}

		Logger.getLogger(Util.class.getName()).log(Level.INFO, "Deleting - " + dir.getName());
		return dir.delete();
	}
}
