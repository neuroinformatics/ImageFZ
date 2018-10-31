package behavior.util;

public class FilenameValidator {
	/**
	 * ファイル名に使えない文字 : \/:*?"<>|
	 * が含まれていないかどうかチェックする。
	 * @param filename 検証するファイル名
	 * @return チェックを通ればtrue,問題があればfalse 
	 */
	public static boolean validate(String filename){
		boolean valid = true;
		valid &= !filename.matches(" *");	// スペースのみ、長さ0の場合を弾く
		if(valid)	// 長さが0のとき対策
			valid &= (filename.charAt(filename.length() - 1) != '.');	// 終端が'.'の場合を弾く
		valid &= !filename.equals(".");		// カレントディレクトリは弾く
		valid &= !filename.contains("..");	// ".." を含むとエラーになるので弾く
		valid &= !filename.contains("\\");
		valid &= !filename.contains("/");
		valid &= !filename.contains(":");
		valid &= !filename.contains("*");
		valid &= !filename.contains("?");
		valid &= !filename.contains("\"");
		valid &= !filename.contains("<");
		valid &= !filename.contains(">");
		valid &= !filename.contains("|");
		return valid;
	}
}