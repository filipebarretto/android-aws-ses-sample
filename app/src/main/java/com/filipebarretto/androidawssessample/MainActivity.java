package com.filipebarretto.androidawssessample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private EditText mToEditText;
    private EditText mCcEditText;
    private EditText mBccEditText;
    private EditText mSubjectEditText;
    private EditText mBodyEditText;
    private Button mSendButton;

    private final static int SUCCESS = 0;
    private final static int ERROR = 1;
    private int result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // CREATES VARIABLES FROM THE RESPECTIVES FIELDS
        // IN LAYOUT FILE
        mToEditText = (EditText) findViewById(R.id.to);
        mCcEditText = (EditText) findViewById(R.id.cc);
        mBccEditText = (EditText) findViewById(R.id.bcc);
        mSubjectEditText = (EditText) findViewById(R.id.subject);
        mBodyEditText = (EditText) findViewById(R.id.body);
        mSendButton = (Button) findViewById(R.id.send);

        // CREATES LISTENER FOR CLICK IN SEND
        // EMAIL BUTTON
        mSendButton.setOnClickListener(sendOnClickListener);
    }

    // LISTENER FOR SEND EMAIL BUTTON
    // THAT CALLS SEND EMAIL FUNCTION
    private View.OnClickListener sendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendEmail();
        }
    };

    // CLEARS ALL DATA FROM ALL INPUT FIELDS
    private void clearFields() {
        mToEditText.setText("");
        mCcEditText.setText("");
        mBccEditText.setText("");
        mSubjectEditText.setText("");
        mBodyEditText.setText("");
    }

    // SENDS EMAIL USING AWS SIMPLE EMAIL SERVICE (SES)
    private void sendEmail() {

        // TODO:
        // REPLACE WITH YOUR IDENTITY POOL AND REGION
        // LOADS CREDENTIALS FROM AWS COGNITO IDENTITY POOL
        CognitoCachingCredentialsProvider credentials = new CognitoCachingCredentialsProvider(
                MainActivity.this, // CONTEXT
                "xx-xxxx-x:xxxxxxxxxxxxxxxxxxxxxxxxx", // IDENTITY POOL ID
                Regions.US_EAST_1 // REGION
        );

        // CREATES SES CLIENT TO MANAGE SENDING EMAIL
        final AmazonSimpleEmailServiceClient ses = new AmazonSimpleEmailServiceClient(credentials);
        ses.setRegion(Region.getRegion(Regions.US_EAST_1));

        // FILLS FIELDS TO SEND EMAIL SUCH AS FROM, TO, CC, BCC
        // SUBJECT AND BODY
        Content subject = new Content(mSubjectEditText.getText().toString());
        Body body = new Body(new Content(mBodyEditText.getText().toString()));
        final Message message = new Message(subject, body);

        // TODO:
        // REPLACE WITH FROM EMAIL AUTHORIZED IN AWS SES
        final String from = "Android AWS SES Sample <noreply@example.com>";
        String to = mToEditText.getText().toString().trim();
        String cc = mCcEditText.getText().toString().trim();
        String bcc = mBccEditText.getText().toString().trim();

        // SPLITS TO, CC AND BCC INPUTS BY , FOR MULTIPLE
        // RECEIVERS AND TAKES CARE OF EMPTY INPUTS
        final Destination destination = new Destination()
                .withToAddresses(to.contentEquals("") ? null : Arrays.asList(to.split("\\s*,\\s*")))
                .withCcAddresses(cc.contentEquals("") ? null : Arrays.asList(cc.split("\\s*,\\s*")))
                .withBccAddresses(bcc.contentEquals("") ? null : Arrays.asList(bcc.split("\\s*,\\s*")));


        // CREATES SEPARATE THREAD TO ATTEMPT TO SEND EMAIL
        Thread sendEmailThread = new Thread(new Runnable() {
            public void run() {
                try {
                    SendEmailRequest request = new SendEmailRequest(from, destination, message);
                    ses.sendEmail(request);

                    result = SUCCESS;

                } catch (Exception e) {
                    result = ERROR;
                }
            }
        });

        // RUNS SEND EMAIL THREAD
        sendEmailThread.start();

        try {
            // WAITS THREAD TO COMPLETE TO ACT ON RESULT
            sendEmailThread.join();

            if (result == SUCCESS) {
                Toast.makeText(MainActivity.this, getString(R.string.email_success), Toast.LENGTH_LONG)
                        .show();
                clearFields();
            } else if (result == ERROR) {
                Toast.makeText(MainActivity.this, getString(R.string.email_error), Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(MainActivity.this, getString(R.string.email_error), Toast.LENGTH_LONG)
                        .show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
