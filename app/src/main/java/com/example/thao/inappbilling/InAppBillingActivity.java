package com.example.thao.inappbilling;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.util.Log;

import com.example.thao.inappbilling.util.IabHelper;
import com.example.thao.inappbilling.util.IabResult;
import com.example.thao.inappbilling.util.Inventory;
import com.example.thao.inappbilling.util.Purchase;

import static com.example.thao.inappbilling.util.IabHelper.BILLING_RESPONSE_RESULT_OK;
import static com.example.thao.inappbilling.util.IabHelper.IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE;
import static com.example.thao.inappbilling.util.IabHelper.ITEM_TYPE_SUBS;
import static com.example.thao.inappbilling.util.IabHelper.getResponseDesc;

public class InAppBillingActivity extends AppCompatActivity {

    Button clickButton;
    Button buyButton;
    private static final String TAG = "THAOcom.example.thao.inappbilling";
    IabHelper mHelper;
    static final String ITEM_SKU = "android.test.purchased";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_app_billing);
        clickButton = (Button)findViewById(R.id.clickButton);
        buyButton = (Button)findViewById(R.id.buyButton);
        clickButton.setEnabled(false);

        String base64EncodedPublicKey ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmrP8pqzpnj662AsRNrgTN3c9K5W978q3PiiwUszHg3w9U6CzHtBkDqbMpfX9l30KczVSoGqTIH01QyyRFyIDgq3kt5ULXrRog0EyZlzpOd++1xP/cZXQvIy82ixCmxUa1y7w/dLxnxC8aOhAQc2JBhLXM90Tb0r/g6MkCe9OH54o/LKD+hHvElLmm6GH3/VzFIeynEt+hVJlJ493nbANq6ElHCO6skMQzydXHwmmkLpyI8RBvceTwKSg+SQkI6gs0ihn9qqUJVvDy/crqtrbrBB8aKbsQ+1Iormpe9cwn5VCNu2kdrXil/FbE+2HQtPTrbGKj9fTRk7BkJliW4OFFwIDAQAB";
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new
                                   IabHelper.OnIabSetupFinishedListener() {
                                       public void onIabSetupFinished(IabResult result)
                                       {
                                           if (!result.isSuccess()) {
                                               Log.d(TAG, "In-app Billing setup failed: " + result);
                                           } else {
                                               Log.d(TAG, "In-app Billing is set up OK");
                                           }
                                       }
                                   });
    }
    public void buttonClicked(View v){
        clickButton.setEnabled(false);
        buyButton.setEnabled(true);
    }

    public void buyClick(View v){
        mHelper.flagEndAsync();
        mHelper.launchPurchaseFlow(this, ITEM_SKU, 10001,
                mPurchaseFinishedListener, "mypurchasetoken1");
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase)
        {
            Log.d("thao","mPurchase finish listener");
            if (result.isFailure()) {
                // Handle error
                Log.d("thao","failure");
                return;

            }
            else if (purchase.getSku().equals(ITEM_SKU)) {
                consumeItem();
                Log.d("thao","buy success");
                clickButton.setEnabled(false);
            }
        }
    };

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {
            Log.d("thao","receive Inventory listener");
            if (result.isFailure()) {
                // Handle failure
                Log.d("thao","inventory failure ");
            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                        mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {
                    Log.d("thao","comsume finish listener");

                    if (result.isSuccess()) {
                        clickButton.setEnabled(true);
                    } else {
                        // handle error
                    }
                }
            };
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        Log.d("thao","onActivityResult mHelper");
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
