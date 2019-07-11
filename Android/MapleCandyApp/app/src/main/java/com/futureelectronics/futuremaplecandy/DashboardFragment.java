package com.futureelectronics.futuremaplecandy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.github.anastr.speedviewlib.util.OnPrintTickLabel;

import java.util.Locale;

import static android.support.v4.view.GravityCompat.apply;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment implements View.OnClickListener{
    private final static String TAG = DashboardFragment.class.getSimpleName();

    private PointerSpeedometer mRpmGauge;
    private int mRpmVal = 0;
    private static final int MAX_RPM_VAL = 1300;

    private float mTemperatureVal = 20.0f;

    private TextView mTxtTemperatureVal;
    private TextView mTxtTemperatureUnits;
    private boolean mTempInF = false;
    private TextView[] mTxtAccelVals = new TextView[3];
    private Button mDAC0Button;
    private Button mDAC1Button;
    //private TextView[] mTxtGyroVals = new TextView[3];

    private TextView mTxtAmbLightVal;
    private int mAmbLightVal = 0;
    private TextView mTxtProxVal;
    private int mProxVal = -1;

    private float[] mAccelVals = {0.0f, 0.0f, 0.0f};
    private float[] mNewAccelVals = {0.0f, 0.0f, 0.0f};
    private byte[]  mNewRawDataVals = {0,0,0};
    private float[] mGyroVals = {0.0f, 0.0f, 0.0f};
    private float[] mNewGyroVals = {0.0f, 0.0f, 0.0f};
    private float mMaxRpmVal = 0;
    private float previousDAC0, previousDAC1;
    public String stringDAC0, stringDAC1;
    public String tempDACString;

    private MapleCandyWriteChar mMapleCandyWriteChar;
    private static InputFilter decimalfilter;

    private enum RadioButtonSelection {
        radioButton_ADC0,
        radioButton_ADC1,
        radioButton_ADC2
    }
    private RadioButtonSelection mRadioButtonSelection;

    private OnPrintTickLabel myPrintTickLabel;

    public DashboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DashboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashboardFragment newInstance() {
        DashboardFragment fragment = new DashboardFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//        }
        previousDAC0 = 0.0f;
        previousDAC1 = 0.0f;
        stringDAC0 = "0";
        stringDAC1 = "0";
        mRadioButtonSelection = RadioButtonSelection.radioButton_ADC0;
        //Filter creation to enter only 1 digit and two decimals for the DACs
        decimalfilter = new InputFilter() {
            final int maxDigitsBeforeDecimalPoint=2;
            final int maxDigitsAfterDecimalPoint=2;

            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                StringBuilder builder = new StringBuilder(dest);
                builder.replace(dstart, dend, source
                        .subSequence(start, end).toString());
                if (!builder.toString().matches(
                        "(([1-9]{1})([0-9]{0,"+(maxDigitsBeforeDecimalPoint-1)+"})?)?(\\.[0-9]{0,"+maxDigitsAfterDecimalPoint+"})?"

                )) {
                    if(source.length()==0)
                        return dest.subSequence(dstart, dend);
                    return "";
                }

                return null;

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mTxtAccelVals[0] = view.findViewById(R.id.txt_accel_x);
        mTxtAccelVals[1] = view.findViewById(R.id.txt_accel_y);
        mTxtAccelVals[2] = view.findViewById(R.id.txt_accel_z);

        setNewAccelGyroVals(true);

        setupRPMGauge(view);

        mDAC0Button = view.findViewById(R.id.button_dac0);
        mDAC0Button.setOnClickListener(this);

        mDAC1Button = view.findViewById(R.id.button_dac1);
        mDAC1Button.setOnClickListener(this);

        // retrieve a reference to the group of radio buttons defined in the dashboard_fragment.xml
        RadioGroup radioGroup_ADCs =view.findViewById(R.id.radiogroup_adcs);
        // attach a listener to the radioGroup to react to the change of selected item/radio button
        radioGroup_ADCs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.radioButton_channel0:
                        mRadioButtonSelection = RadioButtonSelection.radioButton_ADC0;
                        mRpmGauge.setUnit("Volts");
                        break;
                    case R.id.radioButton_channel1:
                        mRadioButtonSelection = RadioButtonSelection.radioButton_ADC1;
                        mRpmGauge.setUnit("Volts");
                        break;
                    case R.id.radioButton_channel2:
                        mRadioButtonSelection = RadioButtonSelection.radioButton_ADC2;
                        mRpmGauge.setUnit("mA");
                        break;
                }

                mRpmGauge.setTickNumber(10);
                setNewAccelGyroVals(false);
            }
        });

        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MapleCandyWriteChar) {
            mMapleCandyWriteChar = (MapleCandyWriteChar) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement MapleCandyWriteChar");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMapleCandyWriteChar = null;
    }

    private void setupRPMGauge(View view)
    {
        mRpmGauge = view.findViewById(R.id.rpm_gauge);
        mRpmGauge.setOnPrintTickLabel((new OnPrintTickLabel() {
            @Override
            public String getTickLabel(int tickPosition, int tick) {
                float temp;
                String tempString = null;
                switch (mRadioButtonSelection){
                    case radioButton_ADC0:
                        temp = tick * (3.3f/100);
                        tempString = String.format(Locale.getDefault(), "%.1f", temp);
                        break;
                    case radioButton_ADC1:
                        temp = tick * (10.0f/100);
                        tempString = String.format(Locale.getDefault(), "%.1f", temp);
                        break;
                    case radioButton_ADC2:
                        temp = tick * (20.0f/100);
                        tempString = String.format(Locale.getDefault(), "%.1f", temp);
                        break;
                }
                return tempString;
                //return null; //null means draw default tick label.
                // also you can return SpannableString to change color, textSize, lines...
            }
        }));

        new Handler().postDelayed(new Runnable() {
            @Override
			public void run() {
                setRPMWidgets();
			}
		}, 20);
    }

    public void onRPMUpdate(int rpm){
        if(mRpmVal != rpm) {
            mRpmVal = rpm;
//            if(mRpmVal > mMaxRpmVal){
                mMaxRpmVal = mRpmVal / 40.95f;
//            }

            AppLog.i(TAG, "RPM is now "+rpm);
            runOnActivityUiThread(new Runnable() {
                @Override
                public void run() {
                    setRPMWidgets();
                }
            });
        }
    }

    public void onRPMUpdateHere(){

    }

    private void setRPMWidgets()
    {
        mRpmGauge.speedTo(mMaxRpmVal, 100);
    }

    public void onUpdateAccelGyro(float[] accelVals, byte[] dataVals)
    {
        if(accelVals!=null && accelVals.length == 3) {
            mNewAccelVals = accelVals;
            mNewRawDataVals = dataVals;
        }

        runOnActivityUiThread(new Runnable() {
            @Override
            public void run() {
                setNewAccelGyroVals(false);
            }
        });

    }

    private void setNewAccelGyroVals(boolean force)
    {
        for(int ix=0; ix<3; ix++){
            if(force || mNewAccelVals[ix] != mAccelVals[ix]){
                mAccelVals[ix] = mNewAccelVals[ix];
                mTxtAccelVals[ix].setText(String.format("%.2f", mAccelVals[ix]));
            }
        }
        int rpm = 0;
        switch(mRadioButtonSelection){
            //ADC0 is from 0 to 3.3V
            case radioButton_ADC0:
                rpm = Utils.bytesTo16bitIntLE(mNewRawDataVals, 0);
                mMaxRpmVal = Utils.bytesTo16bitIntLE(mNewRawDataVals, 0)*(100.0f/4095);
                break;
            //ADC1 is frm 0 to 10V
            case radioButton_ADC1:
                rpm = Utils.bytesTo16bitIntLE(mNewRawDataVals, 2);
                mMaxRpmVal = Utils.bytesTo16bitIntLE(mNewRawDataVals, 2)*(100.0f/4095);
                break;
            //ADC2 is from 0 to 40mA
            case radioButton_ADC2:
                rpm = Utils.bytesTo16bitIntLE(mNewRawDataVals, 4);
                mMaxRpmVal = Utils.bytesTo16bitIntLE(mNewRawDataVals, 4)*(100.0f/4095);
                break;
        }
        if(mRpmVal != rpm) {
            mRpmVal = rpm;
            setRPMWidgets();
        }

    }

    private void runOnActivityUiThread(Runnable runnable){
        FragmentActivity activity = getActivity();
        if(activity != null) {
            activity.runOnUiThread(runnable);
        }
    }
    /**
     * Iterates over an array of tags and applies them to the beginning of the specified
     * Spannable object so that future text appended to the text will have the styling
     * applied to it. Do not call this method directly.
     */
    private static void openTags(Spannable text, Object[] tags) {
        for (Object tag : tags) {
            text.setSpan(tag, 0, 0, Spannable.SPAN_MARK_MARK);
        }
    }
    /**
     * "Closes" the specified tags on a Spannable by updating the spans to be
     * endpoint-exclusive so that future text appended to the end will not take
     * on the same styling. Do not call this method directly.
     */
    private static void closeTags(Spannable text, Object[] tags) {
        int len = text.length();
        for (Object tag : tags) {
            if (len > 0) {
                text.setSpan(tag, 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                text.removeSpan(tag);
            }
        }
    }
    //****The following 3 functions are to change the text color in the AlertDialog
    //****If user wants to use the regular system color text, remove them.
    /**
     * Returns a CharSequence that concatenates the specified array of CharSequence
     * objects and then applies a list of zero or more tags to the entire range.
     *
     * @param content an array of character sequences to apply a style to
     * @param tags the styled span objects to apply to the content
     *        such as android.text.style.StyleSpan
     *
     */
    private static CharSequence apply(CharSequence[] content, Object... tags) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        openTags(text, tags);
        for (CharSequence item : content) {
            text.append(item);
        }
        closeTags(text, tags);
        return text;
    }
    /**
     * Returns a CharSequence that applies a foreground color to the
     * concatenation of the specified CharSequence objects.
     */
    public static CharSequence color(int color, CharSequence... content) {
        return apply(content, new ForegroundColorSpan(color));
    }
	
    public static String pad(String str, int size, char padChar)
    {
        if (str.length() < size)
        {
            char[] temp = new char[size];
            int i = 0;

            while (i < str.length())
            {
                temp[i] = str.charAt(i);
                i++;
            }

            while (i < size)
            {
                temp[i] = padChar;
                i++;
            }

            str = new String(temp);
        }

        return str;
    }

    public void toDo(View v)
    {
        final EditText editText = new EditText(v.getContext());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setFilters(new InputFilter[] {decimalfilter});
        final Activity fragmentActivity=this.getActivity();

        if (v.equals(mDAC0Button)){
            CharSequence mytext = color(Color.rgb(0x00,0xAB,0x67),"DAC0: Enter value 0V to 3.3V");
            new AlertDialog.Builder(v.getContext())
                    .setTitle(mytext)
                    .setView(editText)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //stringDAC0
                            tempDACString = editText.getText().toString();

                            if(mMapleCandyWriteChar != null && !stringDAC0.isEmpty()) {
                                float number = Float.valueOf(editText.getText().toString());
                                if(number <= 3.3f) {
                                    stringDAC0 = tempDACString;
                                    String varStrSend = new String(stringDAC0 + "," + stringDAC1+",");
                                    varStrSend = pad(varStrSend, 20, ',');
                                    mMapleCandyWriteChar.onSetMapleCandy_WriteChar(varStrSend.getBytes());
                                    mMapleCandyWriteChar.onSetMapleCandy_WriteChar(varStrSend.getBytes());
                                    mMapleCandyWriteChar.onSetMapleCandy_WriteChar(varStrSend.getBytes());
                                    Toast.makeText(fragmentActivity,
                                            "Sent: " + editText.getText().toString()+"V",
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(fragmentActivity,
                                            "Value higher than 3.3V",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(fragmentActivity,
                                        "Error Sending BLE GATT Message",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).show();
        }
        if (v.equals(mDAC1Button)){
            CharSequence mytext = color(Color.rgb(0x00,0xAB,0x67),"DAC1: Enter value 0V to 10V");
            new AlertDialog.Builder(v.getContext())
                    .setTitle(mytext)
                    .setView(editText)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            tempDACString = editText.getText().toString();
                            if(mMapleCandyWriteChar != null && !stringDAC1.isEmpty()) {
                                float number = Float.valueOf(editText.getText().toString());
                                if(number <= 10.0f) {
                                    stringDAC1 = tempDACString;
                                    String varStrSend = new String(stringDAC0 + "," + stringDAC1+",");
                                    varStrSend = pad(varStrSend, 20, ',');
                                    mMapleCandyWriteChar.onSetMapleCandy_WriteChar(varStrSend.getBytes());
                                    mMapleCandyWriteChar.onSetMapleCandy_WriteChar(varStrSend.getBytes());
                                    mMapleCandyWriteChar.onSetMapleCandy_WriteChar(varStrSend.getBytes());
                                    Toast.makeText(fragmentActivity,
                                            "Sent: " + editText.getText().toString()+"V",
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(fragmentActivity,
                                            "Value higher than 10V",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(fragmentActivity,
                                        "Error Sending BLE GATT Message",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).show();
        }
    }

    @Override
    public void onClick(View view) {
        toDo(view);
    }

    public interface MapleCandyWriteChar {
        // TODO: Update argument type and name
        void onSetMapleCandy_WriteChar(byte[] data);
    }
}
