// (c) 2017 uchicom
package com.uchicom.cws;

import com.uchicom.util.Parameter;

/**
 * 源泉徴収票出力
 * 
 * @author uchicom: Shigeki Uchiyama
 *
 */
public class Main {

	/**
	 * PDF1ファイルで開いた場合はテンプレートと変換ファイルを出力
	 * PDFとcsvファイルを指定した場合はconf/cws.propertiesに従い処理
	 * PDF,csv,propertiesを指定した場合は、指定のpropertiesに従い処理
	 *
	 * csvの行数分pdfファイルを出力する。または 画像としてpdfに張り付けるかとりあえず出力サイズとフォントを探す
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Parameter parameter = new Parameter(args);

		// 出力ファイル
		if (!parameter.is("outputDir")) {
			parameter.put("outputDir", Constants.DEFAULT_OUTPUT_DIR);
		}
		if (!parameter.is("csv")) {
			parameter.put("csv", Constants.DEFAULT_CSV_FILE);
		}
		if (!parameter.is("template")) {
			parameter.put("template", Constants.DEFAULT_TEMPLATE_FILE);
		}
		if (!parameter.is("config")) {
			parameter.put("config", Constants.DEFAULT_CONFIG_FILE);
		}

		if (parameter.is("init")) {
			Cws.initFile(parameter.getFile("template"));
		} else if (parameter.is("split")) {
			Cws.splitFile(parameter.getFile("template"));
		} else {
			try {
				Cws.execute(parameter.getFile("template"), parameter.getFile("csv"), parameter.getFile("config"),
						parameter.getFile("outputDir"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
