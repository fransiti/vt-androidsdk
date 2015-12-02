/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package id.co.veritrans.sdk.example.gcmutils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import id.co.veritrans.sdk.activities.NotificationActivity;
import id.co.veritrans.sdk.core.Logger;
import id.co.veritrans.sdk.example.R;
import id.co.veritrans.sdk.example.utils.Constants;
import id.co.veritrans.sdk.models.TransactionResponse;

public class VeritransGcmListenerService extends GcmListenerService {

    private static final String TAG = "VeritransGcmListenerService";


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        //String message = data.getString("message");
        //Logger.d(TAG,"data:"+data);
        Logger.d(TAG, "From: " + from);
        //Logger.d(TAG, "Message: " + message);

     /*   if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }*/

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(data);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param data GCM message received.
     */
    private void sendNotification(Bundle data) {
        String statusCode = data.getString(Constants.KEY_STATUS_CODE,"");
        String statusMessage = data.getString(Constants.KEY_STATUS_MESSAGE,"");
        String transactionId = data.getString(Constants.KEY_TRANSACTION_ID,"");
        String orderId = data.getString(Constants.KEY_ORDER_ID,"");
        String grossAmount = data.getString(Constants.KEY_GROSS_AMOUNT,"");
        String paymentType = data.getString(Constants.KEY_PAYMENT_TYPE,"");
        String transactionTime = data.getString(Constants.KEY_TRANSACTION_TIME,"");
        String transactionStatus = data.getString(Constants.KEY_TRANSACTION_STATUS,"");
        TransactionResponse transactionResponse = new TransactionResponse( statusCode,  statusMessage,
                transactionId, orderId,  grossAmount,  paymentType,transactionTime,  transactionStatus);
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.PAYMENT_STATUS,transactionResponse);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.common_ic_googleplayservices)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(statusMessage)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int randomPIN = (int)(Math.random()*9000)+1000;
        notificationManager.notify(randomPIN, notificationBuilder.build());
    }
}
