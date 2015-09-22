package xpshome.net.components;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.security.InvalidParameterException;

/**
 * Created by Christian Poschinger on 18.09.2015.
 * Class to extend the basic TextInputLayout with icons and EditText field to provide a more flexible input field.
 */
public class EditTextAdvanced extends android.support.design.widget.TextInputLayout {
    private static final String defaultButtonText = "show";
    private static final String defaultHint = "enter here";
    private static final String defaultDescription = "enter your data in the above field";

    private static final int IC_LEFT = 0;
    private static final int IC_TOP = 1;
    private static final int IC_RIGHT = 2;
    private static final int IC_BOTTOM = 3;
    private Drawable[] buttonIcons = new Drawable[] {null, null, null, null};

    protected EditText textField;
    protected ImageView icon;
    protected Button imageButton;
    protected LinearLayout inputRow;

    protected TextView descriptionField;
    LinearLayout masterLayout;

    public EditTextAdvanced(Context context) {
        super(context);
        initialize();
    }

    public EditTextAdvanced(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public EditTextAdvanced(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    protected void initialize() {
        textField = new EditText(this.getContext());
        textField.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textField.setHint(defaultHint);

        icon = new ImageView(this.getContext());
        icon.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        icon.setImageResource(android.R.drawable.ic_partial_secure);

        imageButton = new Button(this.getContext());
        imageButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageButton.setText(defaultButtonText);
        imageButton.setCompoundDrawablePadding(10);

        setButtonIcon(IC_LEFT, android.R.drawable.ic_menu_view);

        descriptionField = new TextView(this.getContext());
        descriptionField.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        descriptionField.setText(defaultDescription);

        inputRow = new LinearLayout(getContext());
        inputRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        inputRow.setOrientation(HORIZONTAL);
        inputRow.addView(icon);
        inputRow.addView(textField);
        inputRow.addView(imageButton);

        masterLayout = new LinearLayout(getContext());
        masterLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        masterLayout.setOrientation(VERTICAL);
        masterLayout.addView(inputRow);
        masterLayout.addView(descriptionField);
        this.addView(masterLayout);
    }

    private void setButtonIcons() {
        imageButton.setCompoundDrawables(
                getDrawableFor(IC_LEFT),
                getDrawableFor(IC_TOP),
                getDrawableFor(IC_RIGHT),
                getDrawableFor(IC_BOTTOM));
    }

    private Drawable getDrawableFor(int buttonLocation) {
        if (buttonLocation < IC_LEFT || buttonLocation > IC_RIGHT) {
            return null;
        }
        return buttonIcons[buttonLocation];
    }

    @SuppressWarnings("deprecation")
    private Drawable getDrawableForResID(int resID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(resID, null);
        } else {
            return getResources().getDrawable(resID);
        }
    }

    private final static String EXCP_MSG = "Invalid parameter value. Valid parameter values are between 0 and 3 : e.g: IC_LEFT, IC_TOP, IC_RIGHT, IC_BOTTOM";
    /**
     *
     * @param iconLocation is the position of the icon inside of the button possible values are : IC_LEFT, IC_TOP, IC_RIGHT, IC_BOTTOM
     * @param resID is the resource id of the drawable
     * @throws InvalidParameterException if the iconLocation value is out of the range of valid values
     */
    @SuppressWarnings("unused")
    public void setButtonIcon(int iconLocation, @DrawableRes int resID) throws InvalidParameterException {
        if (iconLocation < IC_LEFT || iconLocation >IC_BOTTOM) {
            throw new InvalidParameterException(EXCP_MSG);
        }
        buttonIcons[iconLocation] = getDrawableForResID(resID);
        setButtonIcons();
    }

    /**
     *
     * @param iconLocation is the position of the icon inside of the button possible values are : IC_LEFT, IC_TOP, IC_RIGHT, IC_BOTTOM
     * @param icon is the Drawable for the icon
     * @throws InvalidParameterException if the iconLocation value is out of the range of valid values
     */
    @SuppressWarnings("unused")
    public void setButtonIcon(int iconLocation, final Drawable icon) throws InvalidParameterException {
        if (iconLocation < IC_LEFT || iconLocation >IC_BOTTOM) {
            throw new InvalidParameterException(EXCP_MSG);
        }
        buttonIcons[iconLocation] = icon;
        setButtonIcons();
    }

    @SuppressWarnings("unused")
    public void setHint(CharSequence text) {
        textField.setHint(text);
    }

    @SuppressWarnings("unused")
    public void setHint(@StringRes int resID) {
        textField.setHint(resID);
    }

    @SuppressWarnings("unused")
    public void setIcon(@DrawableRes int resID) {
        icon.setImageResource(resID);
    }

}
