/*
 * Created on 2006/04/20
 *
 */
package behavior.util.rmstate;


/**
 * @author kazuaki kobayashi
 *
 */
//������ǉ����Ă������̂ł��Ȃ蕡�G�ɂȂ��Ă���
public interface RMState {
	public abstract void intake(RMROI roi);
	public abstract void missing(RMROI roi);
	public abstract void enter(RMROI roi);
	public abstract void exit(RMROI roi);
}