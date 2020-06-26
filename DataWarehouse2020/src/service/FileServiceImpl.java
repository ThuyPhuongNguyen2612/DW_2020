package service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class FileServiceImpl implements FileService {
	static final String NUMBER_REGEX = "^[0-9]+$";

	public FileServiceImpl() {
	}

	@Override
	public String readLines(String value, String delim) {
		String values = "";
		StringTokenizer stoken = new StringTokenizer(value, delim);
		if (stoken.countTokens() > 0) {
			stoken.nextToken();
		}
		int countToken = stoken.countTokens();
		String lines = "(";
		for (int j = 0; j < countToken; j++) {
			String token = stoken.nextToken();
			if (Pattern.matches(NUMBER_REGEX, token)) {
				lines += (j == countToken - 1) ? token.trim() + ")," : token.trim() + ",";
			} else {
				lines += (j == countToken - 1) ? "'" + token.trim() + "')," : "'" + token.trim() + "',";
			}
			values += lines;
			lines = "";
		}
		return values;
	}

	@Override
	public String readValuesTXT(String sourceFile, String delim) {
		String values = "";
		try {
			File file = new File(sourceFile);
			BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while ((line = bReader.readLine()) != null) {
				values += readLines(line, delim);
			}
			bReader.close();
			return values.substring(0, values.length()-1);
		} catch (NoSuchElementException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String readValuesXLSX(String sourceFile) {
		String values = "";
		String value = "";
		try {
			File file = new File(sourceFile);
			FileInputStream fileIn = new FileInputStream(file);
			XSSFWorkbook workBooks = new XSSFWorkbook(fileIn);
			XSSFSheet sheet = workBooks.getSheetAt(0);
			Iterator<Row> rows = sheet.iterator();
			rows.next();
			while (rows.hasNext()) {
				Row row = rows.next();
				Iterator<Cell> cells = row.cellIterator();
				while (cells.hasNext()) {
					Cell cell = cells.next();
					CellType cellType = cell.getCellType();
					switch (cellType) {
					case NUMERIC:
						if (DateUtil.isCellDateFormatted(cell)) {
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
							value += dateFormat.format(cell.getDateCellValue()) + "|";
						} else {
							value += (long) cell.getNumericCellValue() + "|";
						}
						break;
					case STRING:
						value += cell.getStringCellValue() + "|";
						break;
					default:
						break;
					}
				}
				values += readLines(value.substring(0, value.length()), "|");
				value = "";
			}
			workBooks.close();
			fileIn.close();
			return values.substring(0, values.length()-1);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public boolean moveFile(String target_dir, File file) {
		try {
			BufferedInputStream bReader = new BufferedInputStream(new FileInputStream(file));
			BufferedOutputStream bWriter = new BufferedOutputStream(
					new FileOutputStream(target_dir + File.separator + file.getName()));
			byte[] buff = new byte[1024 * 10];
			int data = 0;
			while ((data = bReader.read(buff)) != -1) {
				bWriter.write(buff, 0, data);
			}
			bReader.close();
			bWriter.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			file.delete();
		}
	}
}
