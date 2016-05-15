# Android AWS SES Sample

## About 

Sample Android code using AWS Android SDK with Simple Email Service (SES) to send emails.

### Version
1.0.0

### Table of Contents

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Getting Started](#getting-started)
  - [Setup AWS account](#setup-aws-account)
  - [Setup AWS Cognito](#setup-aws-cognito)
  - [Setup AWS Simple Email Service](#setup-aws-simple-email-service)
  - [Create Android Project](#create-android-project)
    - [Layout file](#layout-file)
    - [Main Activity](#main-activity)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Getting Started

Instructions to create AWS account, configure Cognito and SES, and create Android application. Attention that **charges may apply** to the AWS account.

### Setup AWS account

To create an AWS account, follow instructions in http://docs.aws.amazon.com/lambda/latest/dg/setting-up.html. If you already have an AWS account, you may skip this step.

### Setup AWS Cognito

Sign in to your AWS account at https://signin.aws.amazon.com/console. On the dashboard, choose **Cognito** under **Mobile Services**. Click on **Manage Federated Identities** and then **Create new identity pool**. Enter a name for your identity pool, such as *Android AWS SES Sample*. Then, check the checkbox to **Enable access to unauthenticated identities** and click **Create Pool**.

Now, configure the IAM Role for unauthenticated identities. Click **View Policy Document** and then **Edit**. Replace the Policy with:

```
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "cognito-sync:*",
        "ses:SendEmail"
      ],
      "Resource": [
        "*"
      ]
    }
  ]
}
```

and click **Allow**. Copy the code with get your AWS Credentials to use in the Android Project.

### Setup AWS Simple Email Service

In the AWS service dashboard, choose **SES**. On the menu, choose **Email Addresses** and click **Verify a New Email Address**. Enter the email that you would like to use to send emails, such as noreply@example.com and click **Verify This Email Address**. Login to your email and click on the verification link that you received.

To send emails to any email address, you must open a support ticket demanding a limit increase. Otherwise, you’ll only be able to send to verified email addresses.


### Create Android Project

Open Android Studio and create a new Android Project. Choose **Empty Activity**.

#### Layout file

Edit your activity_main.xml to include input fields for to, cc, and bcc email addresses, subject and body.

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:paddingBottom="@dimen/activity_vertical_margin"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    tools:context="com.filipebarretto.androidawssessample.MainActivity">

    <EditText
        android:id="@+id/to"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="@string/to"
        android:layout_marginTop="@dimen/activity_vertical_margin"/>

    <EditText
        android:id="@+id/cc"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="@string/cc"
        android:layout_marginTop="@dimen/activity_vertical_margin"/>

    <EditText
        android:id="@+id/bcc"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="@string/bcc"
        android:layout_marginTop="@dimen/activity_vertical_margin"/>

    <EditText
        android:id="@+id/subject"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="@string/subject"
        android:layout_marginTop="@dimen/activity_vertical_margin"/>

    <EditText
        android:id="@+id/body"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:hint="@string/body"
        android:inputType="textCapSentences|textMultiLine"
        android:maxLines="4" />

    <Button
        android:id="@+id/send"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/send"/>

</LinearLayout>

```

#### Main Activity

In your Main Activity, create a listener for clicks in the send email button, a function to send emails.

```
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

        // LOADS CREDENTIALS FROM AWS COGNITO IDENTITY POOL
        // REPLACE WITH YOUR IDENTITY POOL AND REGION
        CognitoCachingCredentialsProvider credentials = new CognitoCachingCredentialsProvider(
                MainActivity.this, // CONTEXT
                “xx-xxxx-x:xxxxxxxxxxxxxxxxxxxxxxxxx”, // IDENTITY POOL ID
                Regions.XX_XXXX_X // REGION
        );

        // CREATES SES CLIENT TO MANAGE SENDING EMAIL
        final AmazonSimpleEmailServiceClient ses = new AmazonSimpleEmailServiceClient(credentials);
        ses.setRegion(Region.getRegion(Regions.US_EAST_1));

        // FILLS FIELDS TO SEND EMAIL SUCH AS FROM, TO, CC, BCC
        // SUBJECT AND BODY
        Content subject = new Content(mSubjectEditText.getText().toString());
        Body body = new Body(new Content(mBodyEditText.getText().toString()));
        final Message message = new Message(subject, body);

        // REPLACE WITH FROM EMAIL AUTHORIZED IN AWS SES
        final String from = "Android AWS SES Sample <noreply@example.com>”;
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
```



