package ibcalpha.ibc;

import javax.swing.JFrame;

public class DefaultLoginManager extends LoginManager {

    public DefaultLoginManager(String[] args) {
        fromSettings = true;
        message = "getting username and password from settings";
    }

    private final String message;

    private volatile AbstractLoginHandler loginHandler = null;

    private final boolean fromSettings;

    private volatile String IBAPIUserName;

    private volatile String IBAPIPassword;


    @Override
    public void logDiagnosticMessage(){
        Utils.logToConsole("using default login manager: " + message);
    }

    @Override
    public String IBAPIPassword() {
        String password = Settings.settings().getString("IbPassword", "");
        return password;
    }

    @Override
    public String IBAPIUserName() {
        return Settings.settings().getString("IbLoginId", "");
    }

    @Override
    public JFrame getLoginFrame() {
        return super.getLoginFrame();
    }

    @Override
    public void setLoginFrame(JFrame window) {
        super.setLoginFrame(window);
    }

    @Override
    public AbstractLoginHandler getLoginHandler() {
        return loginHandler;
    }

    @Override
    public void setLoginHandler(AbstractLoginHandler handler) {
        loginHandler = handler;
    }
}
