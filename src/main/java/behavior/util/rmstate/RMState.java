/*
 * Created on 2006/04/20
 *
 */
package behavior.util.rmstate;


/**
 * @author kazuaki kobayashi
 *
 */
//ğŒ‚ğ’Ç‰Á‚µ‚Ä‚¢‚Á‚½‚Ì‚Å‚©‚È‚è•¡G‚É‚È‚Á‚Ä‚¢‚é
public interface RMState {
	public abstract void intake(RMROI roi);
	public abstract void missing(RMROI roi);
	public abstract void enter(RMROI roi);
	public abstract void exit(RMROI roi);
}