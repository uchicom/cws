// (c) 2017 uchicom
package com.uchicom.cws;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDComboBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

import com.uchicom.csve.util.CSVReader;
import com.uchicom.util.ResourceUtil;

/**
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class Cws {

	public static void execute(File templateFile, File csvFile, File configFile, File outputDir) throws Exception {
		Properties conf = ResourceUtil.createProperties(configFile, "utf-8");

		File file = new File(conf.getProperty("font"));
		try (PDDocument doc = PDDocument.load(templateFile);
				TrueTypeCollection collection = new TrueTypeCollection(file);
				CSVReader csvReader = new CSVReader(csvFile, "utf-8");) {
			PDAcroForm form = doc.getDocumentCatalog().getAcroForm();
			PDFont font = PDType0Font.load(doc, collection.getFontByName("MS-Mincho"), true);// Font名から取得
			form.getDefaultResources().add(font);

			// ヘッダ
			String[] heads = csvReader.getNextCsvLine(10, true);
			Map<String, Integer> headMap = new HashMap<>();
			for (int i = 0; i < heads.length; i++) {
				headMap.put(heads[i], i);
			}
			// レコード
			String[] records = null;
			int iLine = 0;
			while ((records = csvReader.getNextCsvLine(heads.length, false)) != null) {
				iLine++;
				COSDictionary acroFormDict = form.getCOSObject();
				acroFormDict.setBoolean(COSName.getPDFName("NeedAppearances"), true);// これがないと右側の日本語が化ける
				form.setDefaultAppearance(form.getDefaultAppearance().replaceAll("^/[^ ]+", "/F9"));

				for (PDField field : form.getFields()) {
					if (!conf.containsKey(field.getFullyQualifiedName())) {
						continue;
					}
					String head = conf.getProperty(field.getFullyQualifiedName());
					if (!headMap.containsKey(head)) {
						continue;
					}
					String value = records[headMap.get(head)];
					if (value == null || "".equals(value)) {
						continue;
					}
					switch (field.getFieldType()) {
					case "Tx":
						if (field instanceof PDTextField) {
							PDTextField text = (PDTextField) field;
							text.setDefaultAppearance(text.getDefaultAppearance().replaceAll("^/[^ ]+", "/F9"));
							field.setValue(value);
						}
						break;
					case "Ch":
						if (field instanceof PDComboBox) {
							PDComboBox combo = (PDComboBox) field;
							combo.setDefaultAppearance(combo.getDefaultAppearance().replaceAll("^/[^ ]+", "/F9"));
							combo.setValue(value);
						}
						break;
					default:
					}
				}
				String year = records[headMap.get(conf.getProperty("year"))];
				String name = records[headMap.get(conf.getProperty("name"))];

				doc.save(new File(outputDir, iLine + "_" + year + "_" + name + ".pdf"));// 年と名前もキーから取得する

				// 画像出力確認
				// form.setNeedAppearances(true);
				// form.refreshAppearances();
				// form.flatten();
				// PDFRenderer renderer = new PDFRenderer(doc);
				// ImageIO.write(renderer.renderImageWithDPI(0, 600), "PNG", new
				// File("output.png"));
			}
		}
	}

	/**
	 * 0スタート
	 * 
	 * @param index
	 * @return
	 */
	public static String getColumnName(int index) {
		if (index / 26 > 0) {
			return new String(new char[] { (char) ('A' + (index / 26) - 1), (char) ('A' + (index % 26)) });
		} else {
			return new String(new char[] { (char) ('A' + index) });
		}
	}

	/**
	 * 0スタート
	 * 
	 * @param columnName
	 * @return
	 */
	public static int getColumnIndex(String columnName) {
		if (columnName.length() > 1) {
			int up = columnName.charAt(0) - 'A';
			int down = columnName.charAt(1) - 'A';
			return (up + 1) * 26 + down;
		} else {
			int down = columnName.charAt(0) - 'A';
			return down;
		}
	}

	public static void splitFile(File file) {

		try (PDDocument doc = PDDocument.load(file)) {
			for (int i = doc.getNumberOfPages() - 1; i > 0; i--) {
				doc.removePage(i);
			}
			doc.save(new File("template", "ws.pdf"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void initFile(File file) {
		Properties transfer = new Properties();
		try (PDDocument doc = PDDocument.load(file)) {
			PDAcroForm form = doc.getDocumentCatalog().getAcroForm();

			// 出現
			form.setNeedAppearances(true);
			StringBuffer strBuff = new StringBuffer(1024);
			List<PDField> fieldList = form.getFields();
			for (int i = 0; i < fieldList.size(); i++) {
				PDField field = fieldList.get(i);
				String columnName = getColumnName(i);
				if (i != 0) {
					strBuff.append(",");
				}
				strBuff.append(columnName);
				field.setValue(String.valueOf(i + 1));
				transfer.setProperty(field.getFullyQualifiedName(), columnName);
				// 読み取り専用
				// field.setReadOnly(true);
			}
			// 変換ファイル保存
			File out = new File("output");
			out.mkdirs();
			transfer.store(new FileOutputStream(new File("conf", "cws2.properties")), "cws");
			// テンプレート
			Files.write(new File(out, "template.csv").toPath(), strBuff.toString().getBytes("utf-8"),
					StandardOpenOption.CREATE);
			doc.getDocumentCatalog().setAcroForm(form);
			doc.save(new File(out, "index.pdf"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
