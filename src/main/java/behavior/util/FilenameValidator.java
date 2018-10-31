package behavior.util;

public class FilenameValidator {
	/**
	 * �t�@�C�����Ɏg���Ȃ����� : \/:*?"<>|
	 * ���܂܂�Ă��Ȃ����ǂ����`�F�b�N����B
	 * @param filename ���؂���t�@�C����
	 * @return �`�F�b�N��ʂ��true,��肪�����false 
	 */
	public static boolean validate(String filename){
		boolean valid = true;
		valid &= !filename.matches(" *");	// �X�y�[�X�̂݁A����0�̏ꍇ��e��
		if(valid)	// ������0�̂Ƃ��΍�
			valid &= (filename.charAt(filename.length() - 1) != '.');	// �I�[��'.'�̏ꍇ��e��
		valid &= !filename.equals(".");		// �J�����g�f�B���N�g���͒e��
		valid &= !filename.contains("..");	// ".." ���܂ނƃG���[�ɂȂ�̂Œe��
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