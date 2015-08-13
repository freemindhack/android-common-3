
package common.utils;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;


import com.za.smartlock.customer.R;
import common.datastructure.MyQueue;


public class NumberKeyboard extends LinearLayout implements OnClickListener {
	public NumberKeyboard (Context context) {
		super(context);
		init(context);
	}


	public NumberKeyboard (Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}


	public void setOnTextChangedListener (
		OnKeyPressedListener onKeyPressedListener) {
		this.onKeyPressedListener = onKeyPressedListener;
	}


	@Override
	public void onClick (View widget) {
		if (widget.getId() == R.id.numberkeyboard_1) {
			numberQueue.enqueue(49);
		} else if (widget.getId() == R.id.numberkeyboard_2) {
			numberQueue.enqueue(50);
		} else if (widget.getId() == R.id.numberkeyboard_3) {
			numberQueue.enqueue(51);
		} else if (widget.getId() == R.id.numberkeyboard_4) {
			numberQueue.enqueue(52);
		} else if (widget.getId() == R.id.numberkeyboard_5) {
			numberQueue.enqueue(53);
		} else if (widget.getId() == R.id.numberkeyboard_6) {
			numberQueue.enqueue(54);
		} else if (widget.getId() == R.id.numberkeyboard_7) {
			numberQueue.enqueue(55);
		} else if (widget.getId() == R.id.numberkeyboard_8) {
			numberQueue.enqueue(56);
		} else if (widget.getId() == R.id.numberkeyboard_9) {
			numberQueue.enqueue(57);
		} else if (widget.getId() == R.id.numberkeyboard_0) {
			numberQueue.enqueue(48);
		} else if (widget.getId() == R.id.numberkeyboard_delete) {
			numberQueue.enqueue(8);
		}
		if (onKeyPressedListener != null) {
			onKeyPressedListener.onKeyPressed(this.numberQueue.dequeue());
		}
	}


	private void init (Context context) {
		LayoutInflater inflater = (LayoutInflater) context
			.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.linearlayout_numkeyboard, this);
		num1 = (ImageButton) findViewById(R.id.numberkeyboard_1);
		num2 = (ImageButton) findViewById(R.id.numberkeyboard_2);
		num3 = (ImageButton) findViewById(R.id.numberkeyboard_3);
		num4 = (ImageButton) findViewById(R.id.numberkeyboard_4);
		num5 = (ImageButton) findViewById(R.id.numberkeyboard_5);
		num6 = (ImageButton) findViewById(R.id.numberkeyboard_6);
		num7 = (ImageButton) findViewById(R.id.numberkeyboard_7);
		num8 = (ImageButton) findViewById(R.id.numberkeyboard_8);
		num9 = (ImageButton) findViewById(R.id.numberkeyboard_9);
		num0 = (ImageButton) findViewById(R.id.numberkeyboard_0);
		delete = (ImageButton) findViewById(R.id.numberkeyboard_delete);
		num1.setOnClickListener(this);
		num2.setOnClickListener(this);
		num3.setOnClickListener(this);
		num4.setOnClickListener(this);
		num5.setOnClickListener(this);
		num6.setOnClickListener(this);
		num7.setOnClickListener(this);
		num8.setOnClickListener(this);
		num9.setOnClickListener(this);
		num0.setOnClickListener(this);
		delete.setOnClickListener(this);
	}


	private ImageButton num1, num2, num3, num4, num5, num6, num7, num8, num9,
		num0, delete;

	private OnKeyPressedListener onKeyPressedListener;

	private MyQueue <Integer> numberQueue = new MyQueue <Integer>();
}
