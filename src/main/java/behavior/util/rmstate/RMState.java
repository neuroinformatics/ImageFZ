/*
 * Created on 2006/04/20
 *
 */
package behavior.util.rmstate;


/**
 * @author kazuaki kobayashi
 *
 */
//条件を追加していったのでかなり複雑になっている
public interface RMState {
	public abstract void intake(RMROI roi);
	public abstract void missing(RMROI roi);
	public abstract void enter(RMROI roi);
	public abstract void exit(RMROI roi);
}