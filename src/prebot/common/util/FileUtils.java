package prebot.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
	
//	public static final String LOG_FILE_NAME = BOT_INFO.BOT_NAME + "_LastGameLog.dat"; /// 로그 파일 이름
	private static final String READ_DIRECTORY = "bwapi-data\\read\\"; /// 읽기 파일 경로
	private static final String WRITE_DIRECTORY = "bwapi-data\\write\\"; /// 쓰기 파일 경로

	// BasicBot 1.1 Patch Start ////////////////////////////////////////////////
	// appendTextToFile 등 메소드를 static 으로 수정

	/// 로그 유틸
	public static void appendTextToFile(final String logFile, final String msg) {
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(WRITE_DIRECTORY + logFile), true));
			bos.write(msg.getBytes());
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/// 로그 유틸
	public static void overwriteToFile(final String logFile, final String msg) {
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(WRITE_DIRECTORY + logFile)));
			bos.write(msg.getBytes());
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/// 파일 유틸 - 텍스트 파일을 읽어들인다
	public static String readFile(final String filename) {
		BufferedInputStream bis;
		StringBuilder sb = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(new File(READ_DIRECTORY + filename)));
			sb = new StringBuilder();

			while (bis.available() > 0) {
				sb.append((char) bis.read());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	// BasicBot 1.1 Patch End //////////////////////////////////////////////////

}