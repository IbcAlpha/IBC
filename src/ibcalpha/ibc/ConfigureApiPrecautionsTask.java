// This file is part of IBC.
// Copyright (C) 2004 Steven M. Kearns (skearns23@yahoo.com )
// Copyright (C) 2004 - 2023 Richard L King (rlking@aultan.com)
// For conditions of distribution and use, see copyright notice in COPYING.txt

// IBC is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// IBC is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with IBC.  If not, see <http://www.gnu.org/licenses/>.

package ibcalpha.ibc;

import javax.swing.JCheckBox;
import javax.swing.JDialog;

public class ConfigureApiPrecautionsTask implements ConfigurationAction {
    
    private JDialog configDialog;
    
    private final String bypassOrderPrecautions;
    private final String bypassBondWarning;
    private final String bypassNegativeYieldToWorstConfirmation;
    private final String bypassCalledBondWarning;
    private final String bypassSameActionPairTradeWarning;
    private final String bypassPriceBasedVolatilityRiskWarning;
    private final String bypassUSStocksMarketDataInSharesWarning;
    private final String bypassRedirectOrderWarning;
    private final String bypassNoOverfillProtectionPrecaution;


    ConfigureApiPrecautionsTask (
            String bypassOrderPrecautions,
            String bypassBondWarning,
            String bypassNegativeYieldToWorstConfirmation,
            String bypassCalledBondWarning,
            String bypassSameActionPairTradeWarning,
            String bypassPriceBasedVolatilityRiskWarning,
            String bypassUSStocksMarketDataInSharesWarning,
            String bypassRedirectOrderWarning,
            String bypassNoOverfillProtectionPrecaution) {
        this.bypassOrderPrecautions = bypassOrderPrecautions;
        this.bypassBondWarning = bypassBondWarning;
        this.bypassNegativeYieldToWorstConfirmation = bypassNegativeYieldToWorstConfirmation;
        this.bypassCalledBondWarning = bypassCalledBondWarning;
        this.bypassSameActionPairTradeWarning = bypassSameActionPairTradeWarning;
        this.bypassPriceBasedVolatilityRiskWarning = bypassPriceBasedVolatilityRiskWarning;
        this.bypassUSStocksMarketDataInSharesWarning = bypassUSStocksMarketDataInSharesWarning;
        this.bypassRedirectOrderWarning = bypassRedirectOrderWarning;
        this.bypassNoOverfillProtectionPrecaution = bypassNoOverfillProtectionPrecaution;
    }

    @Override
    public void initialise(JDialog configDialog) {
        this.configDialog = configDialog;
    }

    @Override
    public void run() {
        try {
            Utils.selectConfigSection(configDialog, new String[] {"API", "Precautions"});

            doSetting("Bypass Order Precautions for API Orders", bypassOrderPrecautions.toLowerCase());
            doSetting("Bypass Bond warning for API Orders", bypassBondWarning.toLowerCase());
            doSetting("Bypass negative yield to worst confirmation for API Orders", bypassNegativeYieldToWorstConfirmation.toLowerCase());
            doSetting("Bypass Called Bond warning for API Orders", bypassCalledBondWarning.toLowerCase());
            doSetting("Bypass \"same action pair trade\" warning for API orders.", bypassSameActionPairTradeWarning.toLowerCase());
            doSetting("Bypass price-based volatility risk warning for API Orders", bypassPriceBasedVolatilityRiskWarning.toLowerCase());
            doSetting("Bypass US Stocks market data in shares warning for API Orders", bypassUSStocksMarketDataInSharesWarning.toLowerCase());
            doSetting("Bypass Redirect Order warning for Stock API Orders", bypassRedirectOrderWarning.toLowerCase());
            doSetting("Bypass No Overfill Protection precaution for destinations where implied natively", bypassNoOverfillProtectionPrecaution.toLowerCase());
            
        } catch (IbcException e) {
            Utils.exitWithError(ErrorCodes.UNHANDLED_EXCEPTION, "Can't find API - Precautions settings");
        }
        
    }

    private void doSetting(String checkBoxText, String setting) {
        switch (setting) {
            case "":
                return;
            case "yes":
            case "no":
                break;
            default:
                Utils.exitWithError(ErrorCodes.INVALID_SETTING_VALUE, "Invalid setting value for '" + checkBoxText + "'" );
        }
        
        JCheckBox cb = SwingUtils.findCheckBox(configDialog, checkBoxText);
        if (cb == null) {
            Utils.logToConsole("Checkbox '" + checkBoxText + "' not found");
            return;
        }
        
        if (setting.equals("yes")) {
            if (cb.isSelected()) {
                Utils.logToConsole("'" + checkBoxText + "' is already selected");
            } else {
                cb.setSelected(true);
                Utils.logToConsole("'" + checkBoxText + "' is now selected");
            }
        } else if (setting.equals("no")) {
            if (!cb.isSelected()) {
                Utils.logToConsole("'" + checkBoxText + "' is already unselected");
            } else {
                cb.setSelected(false);
                Utils.logToConsole("'" + checkBoxText + "' is now unselected");
            }
        }
    }
}
