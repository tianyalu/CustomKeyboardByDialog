
package com.sty.learn.keyboard.dialog;

import java.util.Random;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * 密码键盘，字母、数字随机排列显示
 */
public class LPKeyBoard extends LinearLayout {

    private PopupWindow popWindow_;
    Dialog dlg_;
    boolean isShow_ = false;
    LPImfView imfView;
    MainActivity bv_;
    EditText tempEdit_;
    buttonOk btnOK_; // 确定按钮
    // 判断当前输入框输入类型是否是数字
    boolean inputTypeNumber_ = false;
    // cs模板定义的输入长度限制
    int maxSize;
    private Vibrator vibrator_;
    private static final long VIBRATE_DURATION = 30L;

    public LPKeyBoard(Context context, LPTextField text) {

        super(context);
        bv_ = (MainActivity) context;
        btnOK_ = new buttonOk(context, false);
        tempEdit_ = new EditText(context);
        tempEdit_.setTextSize(15);
        tempEdit_.setHeight(text.getHeight() - 10);
        // tempEdit_.setWidth(LPUtils.screenWidth_);
        tempEdit_.setFocusable(false);
        tempEdit_.setBackgroundResource(R.drawable.bg_edittext);
        vibrator_ = (Vibrator) bv_.getSystemService(Context.VIBRATOR_SERVICE);
        tempEdit_.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        if (text.getInputType() == android.text.InputType.TYPE_CLASS_NUMBER) {
            inputTypeNumber_ = true;
        }
        if (inputTypeNumber_) {
            tempEdit_.setLayoutParams(new LayoutParams(
                    LPUtils.screenWidth_, LayoutParams.WRAP_CONTENT));
            this.addView(tempEdit_);
        } else {
            tempEdit_.setLayoutParams(new LayoutParams(
                    LPUtils.screenWidth_ * 4 / 5, LayoutParams.WRAP_CONTENT));
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT));
            ll.addView(tempEdit_);
            LayoutParams lp = new LayoutParams(LPUtils.screenWidth_ / 5
                    - LPUtils.getScaledValue(6), text.getHeight() - 10);
            ll.addView(btnOK_, lp);
            lp.setMargins(LPUtils.getScaledValue(4), 0, 0, 0);
            this.addView(ll);
        }

        if (null != text.getHint()) {
            tempEdit_.setHint(text.getHint());
        }
        imfView = new LPImfView(context, text);
        this.addView(imfView);

        requestFocus();
        this.setClickable(true);
        this.setBackgroundResource(R.drawable.keyboard_bg);
        this.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        this.setOrientation(LinearLayout.VERTICAL);
    }

    class LPImfView extends TextView {

        private int PAINT_SIZE = 22;
        private int BORDER = LPUtils.getScaledValue(2);
        private int BORDER_TOP = LPUtils.getScaledValue(4);
        private int BORDER_CENTER = LPUtils.getScaledValue(5);
        private int colW_one = LPUtils.getScaledValue(4);
        private int colW_two = LPUtils.getScaledValue(6);
        private int BORDER_TWO = LPUtils.getScaledValue(6);
        private int windownShow_W = LPUtils.getScaledValue(1);
        private int BORDER_NUMBER_CENTER = LPUtils.getScaledValue(4);
        private EditText edit_;
        private static final int FONT_H = 5;
        int height_, width_, qwertyW_, qwertyH_, numW_, numH_;
        private Paint paint_, textPaint_;
        // 标记按键行数 、 列数
        private int key_row = -1;
        private int key_column = -1;

        private long keyDownTime;

        // 用来保存字符串的
        private StringBuffer textBuffer_;
        private Bitmap keyBoard_bg, keyBoard_one, keyBoard_one_down,
                keyBoard_jsbank, keyBoard_gray_enter, keyBoard_del_down,
                keyBoard_white_enter, keyBoard_gray_del, keyBoard_shift_normal,
                keyBoard_shift_down, keyBoard_jsbank_logo, keyboard_num,
                keyboard_num_down, keyboard_num_del, keyboard_num_downdel,
                keyboard_abc_up, keyboard_abc_down;
        private Bitmap keyOne_, keyTwo_, keyThree_, keyFour_;
        private int[] resultAbc, resultNum;

        // 键盘字母表
        private final char[] keyAbc_ = {
                'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's',
                'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b',
                'n', 'm'
        };

        /*
         * isABC_:true-大写，false-小写 isNum_:true-数字，false-字母
         * keyEnter_:true-按下，false-抬起 isDel_: true - 按下，false-抬起
         * isDelete_:true-最后一个字符显示为*，false-最后一个字符显示实际字符
         */
        boolean isABC_, isNum_, keyEnter_, isDel_, isSpace_, isDelete_;
        // 标记字母、数字键第一行的横坐标位置
        private int column1 = -1;
        // 标记字母键第二行的横坐标位置
        private int column2 = -1;
        // 标记字母键第三行的横坐标位置
        private int column3 = -1;
        // 标记数字键第二行的横坐标位置
        private int column4 = -1;
        // 标记数字键第三行的横坐标位置
        private int column5 = -1;

        public LPImfView(Context context, LPTextField text) {

            super(context);
            edit_ = text;
            maxSize = text.maxSize_;
            setFocusable(true);
            requestFocus();
            init();
        }

        private void init() {
            // resultAbc = getRandomNumber(26);
            resultAbc = new int[26];
            for (int i = 0; i < 26; i++) {
                resultAbc[i] = i;
            }
            resultNum = getRandomNumber(10);

            String text = edit_.getText().toString();
            if (text != null) {
                textBuffer_ = new StringBuffer(text);
                tempEdit_.setText(getStringBuffer(textBuffer_, false));
                // edit_.setText("");
            }

            textBuffer_ = new StringBuffer();

            if (inputTypeNumber_) {
                // 如果输入类型为数字的话，弹出的时数字键盘
                isNum_ = true;
            }
            PAINT_SIZE = LPUtils.getScaledValue(PAINT_SIZE);
            paint_ = new Paint();
            paint_.setAntiAlias(true);
            paint_.setTextSize(PAINT_SIZE);

            textPaint_ = new Paint();
            textPaint_.setAntiAlias(true);
            // textPaint_.setTypeface(Typeface.DEFAULT_BOLD);
            textPaint_.setTextSize(PAINT_SIZE);

            keyBoard_bg = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_bg);

            keyBoard_jsbank_logo = BitmapFactory.decodeResource(getResources(),
                    R.drawable.logo);
            keyBoard_jsbank = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_czbank);

            keyBoard_one = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_one);
            keyBoard_one_down = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_one_down);

            keyBoard_shift_normal = BitmapFactory.decodeResource(
                    getResources(), R.drawable.keyboard_shift_normal);
            keyBoard_shift_down = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_shift_down);

            keyBoard_gray_del = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_gray_del);
            keyBoard_del_down = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_gray_del_down);

            keyBoard_gray_enter = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_gray_enter);
            keyBoard_white_enter = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_white_enter);

            keyboard_num = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_num);

            keyboard_num_down = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_num_down);

            keyboard_num_del = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_num_del);

            keyboard_num_downdel = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_num_downdel);

            keyboard_abc_up = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_abc_up);

            keyboard_abc_down = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_abc_down);

            // 根据屏幕大小缩放图片
            int w = LPUtils.screenWidth_ / 3 - BORDER_NUMBER_CENTER;
            int h = ((keyBoard_bg.getHeight() - BORDER_TOP) / 4) - BORDER_TOP;
            keyboard_num = Bitmap.createScaledBitmap(keyboard_num, w, h, true);
            keyboard_num_down = Bitmap.createScaledBitmap(keyboard_num_down, w,
                    h, true);
            keyboard_num_del = Bitmap.createScaledBitmap(keyboard_num_del, w,
                    h, true);
            keyboard_num_downdel = Bitmap.createScaledBitmap(
                    keyboard_num_downdel, w, h, true);
            keyboard_abc_up = Bitmap.createScaledBitmap(keyboard_abc_up, w, h,
                    true);
            keyboard_abc_down = Bitmap.createScaledBitmap(keyboard_abc_down, w,
                    h, true);

            // 缩放字母键的图片
            int abc_W = (LPUtils.screenWidth_ / 10 - colW_one);
            int abc_H = ((keyBoard_bg.getHeight() - BORDER_TOP) / 4)
                    - BORDER_TOP;
            keyBoard_one = Bitmap.createScaledBitmap(keyBoard_one, abc_W,
                    abc_H, true);
            keyBoard_one_down = Bitmap.createScaledBitmap(keyBoard_one_down,
                    abc_W, abc_H, true);
            keyBoard_shift_normal = Bitmap.createScaledBitmap(
                    keyBoard_shift_normal, abc_W + abc_W / 2, abc_H, true);
            keyBoard_shift_down = Bitmap.createScaledBitmap(
                    keyBoard_shift_down, abc_W + abc_W / 2, abc_H, true);
            keyBoard_gray_del = Bitmap.createScaledBitmap(keyBoard_gray_del,
                    abc_W << 1, abc_H, true);
            keyBoard_del_down = Bitmap.createScaledBitmap(keyBoard_del_down,
                    abc_W << 1, abc_H, true);
            keyBoard_gray_enter = Bitmap.createScaledBitmap(
                    keyBoard_gray_enter, abc_W << 1, abc_H, true);
            keyBoard_white_enter = Bitmap.createScaledBitmap(
                    keyBoard_white_enter, abc_W << 1, abc_H, true);
            keyBoard_jsbank = Bitmap.createScaledBitmap(keyBoard_jsbank,
                    (abc_W + colW_one) * 6, abc_H, true);
            keyBoard_jsbank_logo = Bitmap.createScaledBitmap(
                    keyBoard_jsbank_logo, (abc_W + colW_one) * 4, abc_H
                            - LPUtils.getScaledValue(8), true);

            // 弹出键盘的高度
            height_ = keyBoard_bg.getHeight();
            // 弹出键盘的宽度
            width_ = LPUtils.screenWidth_;
            // 字母键的宽度
            qwertyW_ = keyBoard_one.getWidth();
            // 字母键的高度
            qwertyH_ = keyBoard_one.getHeight();

            // 数字键的宽度
            numW_ = keyboard_num.getWidth();
            // 数字键的高度
            numH_ = keyboard_num.getHeight();

            BORDER_TWO = BORDER + qwertyW_ / 2;
            colW_two = qwertyW_ / 3;
            windownShow_W = Math.abs((qwertyW_ - LPUtils.screenWidth_ / 8) / 2);

            // colW_one = LPUtils.getScaledValue(colW_one);
            // colW_two = LPUtils.getScaledValue(colW_two);
            // BORDER_TWO = LPUtils.getScaledValue(BORDER_TWO);
            // BORDER = LPUtils.getScaledValue(BORDER);
            // BORDER_TOP = LPUtils.getScaledValue(BORDER_TOP);
            // BORDER_CENTER = LPUtils.getScaledValue(BORDER_CENTER);

            LayoutParams lp = (LayoutParams) this
                    .getLayoutParams();

            if (lp == null) {
                setLayoutParams(new LayoutParams(width_, height_));
            } else {
                lp.height = height_;
                lp.width = LPUtils.screenWidth_;
                setLayoutParams(lp);
            }
        }

        public void onDraw(Canvas g) {

            /*
             * 画键盘的背景
             */
            // g.drawBitmap(keyBoard_bg, 0, 0, paint_);
            drawKeyBoard(g);
        }

        /*
         * 画出字母键盘的布局键
         */
        private void drawKeyBoard(Canvas g) {

            /*
             * 使得原先的按下去效果无效
             */
            // column1 = -1;
            // column2 = -1;
            // column3 = -1;
            // column4 = -1;
            // column5 = -1;

            /*
             * 画字母键的第一行
             */
            textPaint_.setColor(Color.BLACK);

            int startX = 0;
            int startY = 0;
            String temp;

            if (isNum_) {
                textPaint_.setTextSize(PAINT_SIZE + 2);
                // 画数字键 （4行3列）
                startX = (int) (((numW_ + BORDER_NUMBER_CENTER) - textPaint_
                        .measureText("1")) / 2);
                startY = (int) (((numH_ + BORDER_NUMBER_CENTER) >> 1) + FONT_H + LPUtils
                        .getScaledValue(3));
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        if (key_row == i) {
                            if (j == key_column) {
                                keyOne_ = keyboard_num_down;
                            } else {
                                keyOne_ = keyboard_num;
                            }
                        } else {
                            keyOne_ = keyboard_num;
                        }
                        g.drawBitmap(keyOne_, BORDER
                                + (numW_ + BORDER_NUMBER_CENTER) * j,
                                BORDER_TOP
                                        + ((numH_ + BORDER_NUMBER_CENTER) * i),
                                paint_);

                        g.drawText(String.valueOf(resultNum[j + (i * 3)]),
                                startX + BORDER
                                        + (numW_ + BORDER_NUMBER_CENTER) * j,
                                startY + BORDER_TOP
                                        + ((numH_ + BORDER_NUMBER_CENTER) * i),
                                textPaint_);

                    }
                }

                // 画第4行功能键
                if (key_row == 3 && key_column == 0) {
                    g.drawBitmap(keyboard_abc_down, BORDER, BORDER_TOP
                            + ((numH_ + BORDER_NUMBER_CENTER) * 3), paint_);
                } else {
                    g.drawBitmap(inputTypeNumber_ ? keyboard_num_down
                            : keyboard_abc_up, BORDER, BORDER_TOP
                            + ((numH_ + BORDER_NUMBER_CENTER) * 3), paint_);
                }
                // textPaint_.setColor(Color.WHITE);
                g.drawText(!inputTypeNumber_ ? "ABC" : "确定", BORDER
                        + (numW_ - textPaint_.measureText("ABC")) / 2,
                        BORDER_TOP + ((numH_ + BORDER_NUMBER_CENTER) * 3)
                                + startY, textPaint_);

                if (key_row == 3 && key_column == 1) {
                    g.drawBitmap(keyboard_num_down, BORDER + numW_
                            + BORDER_NUMBER_CENTER, BORDER_TOP
                            + ((numH_ + BORDER_NUMBER_CENTER) * 3), paint_);
                } else {
                    g.drawBitmap(keyboard_num, BORDER + numW_
                            + BORDER_NUMBER_CENTER, BORDER_TOP
                            + ((numH_ + BORDER_NUMBER_CENTER) * 3), paint_);
                }
                textPaint_.setColor(Color.BLACK);
                g.drawText(String.valueOf(resultNum[9]), startX + BORDER
                        + numW_ + BORDER_NUMBER_CENTER, BORDER_TOP
                        + ((numH_ + BORDER_NUMBER_CENTER) * 3) + startY,
                        textPaint_);

                if (key_row == 3 && key_column == 2) {
                    g.drawBitmap(inputTypeNumber_ ? keyboard_num_del
                            : keyboard_num_downdel, BORDER
                            + ((numW_ + BORDER_NUMBER_CENTER) << 1), BORDER_TOP
                            + ((numH_ + BORDER_NUMBER_CENTER) * 3), paint_);
                    delEditAgainValue();
                } else {
                    g.drawBitmap(inputTypeNumber_ ? keyboard_num_downdel
                            : keyboard_num_del, BORDER
                            + ((numW_ + BORDER_NUMBER_CENTER) << 1), BORDER_TOP
                            + ((numH_ + BORDER_NUMBER_CENTER) * 3), paint_);
                }

            } else {
                textPaint_.setTextSize(PAINT_SIZE);
                startY = (int) (BORDER_TOP + qwertyH_ / 2 + LPUtils
                        .getScaledValue(5));
                for (int i = 0; i <= 9; i++) {
                    if (!isABC_) {
                        temp = String.valueOf(keyAbc_[resultAbc[i]]);
                    } else {
                        temp = String.valueOf(keyAbc_[resultAbc[i]])
                                .toUpperCase();
                    }
                    startX = getIntX(BORDER, String.valueOf(temp))
                            + (colW_one + qwertyW_) * i;
                    if (column1 >= 0) {
                        if (i == column1) {
                            keyOne_ = keyBoard_one_down;
                        } else {
                            keyOne_ = keyBoard_one;
                        }
                    } else {
                        keyOne_ = keyBoard_one;
                    }
                    g.drawBitmap(keyOne_, BORDER + (qwertyW_ + colW_one) * i,
                            BORDER_TOP, paint_);

                    if (!isABC_) {
                        g.drawText(String.valueOf(keyAbc_[resultAbc[i]]),
                                startX, startY, textPaint_);
                    } else {
                        g.drawText(String.valueOf(keyAbc_[resultAbc[i]])
                                .toUpperCase(), startX, startY, textPaint_);
                    }
                }

                /*
                 * 画字母键的第二行
                 */
                int startX1 = 0;
                String temp1;
                for (int i = 0; i <= 8; i++) {
                    if (!isABC_) {
                        temp1 = String.valueOf(keyAbc_[resultAbc[10 + i]]);
                    } else {
                        temp1 = String.valueOf(keyAbc_[resultAbc[10 + i]])
                                .toUpperCase();
                    }
                    startX1 = getIntX(BORDER_TWO, temp1)
                            + (colW_one + qwertyW_) * i;
                    if (column2 >= 0) {
                        if (column2 == i) {
                            keyTwo_ = keyBoard_one_down;
                        } else {
                            keyTwo_ = keyBoard_one;
                        }
                    } else {
                        keyTwo_ = keyBoard_one;
                    }
                    g.drawBitmap(keyTwo_, BORDER_TWO + (qwertyW_ + colW_one)
                            * i, BORDER_CENTER + BORDER_TOP + qwertyH_, paint_);

                    if (!isABC_) {
                        g.drawText(String.valueOf(keyAbc_[resultAbc[10 + i]]),
                                startX1, startY + BORDER_CENTER + qwertyH_,
                                textPaint_);
                    } else {
                        g.drawText(String.valueOf(keyAbc_[resultAbc[10 + i]])
                                .toUpperCase(), startX1, startY + BORDER_CENTER
                                + qwertyH_, textPaint_);
                    }
                }
                /*
                 * 画字母键的第三行
                 */
                int startX2 = 0;
                String temp2;
                for (int i = 0; i <= 6; i++) {
                    if (!isABC_) {
                        temp2 = String.valueOf(keyAbc_[resultAbc[19 + i]]);
                    } else {
                        temp2 = String.valueOf(keyAbc_[resultAbc[19 + i]])
                                .toUpperCase();
                    }
                    startX2 = getIntX(BORDER + qwertyW_ - qwertyW_ / 4, temp2)
                            + (colW_one + qwertyW_) * i;
                    if (column3 >= 0) {
                        if (i == column3) {
                            keyThree_ = keyBoard_one_down;
                        } else {
                            keyThree_ = keyBoard_one;
                        }
                    } else {
                        keyThree_ = keyBoard_one;
                    }
                    g.drawBitmap(keyThree_, BORDER + qwertyW_ - qwertyW_ / 4
                            + (qwertyW_ + colW_one) * i, BORDER_TOP + 2
                            * BORDER_CENTER + 2 * qwertyH_, paint_);

                    if (!isABC_) {
                        g.drawText(String.valueOf(keyAbc_[resultAbc[19 + i]]),
                                startX2, startY + 2
                                        * (BORDER_CENTER + qwertyH_),
                                textPaint_);
                    } else {
                        g.drawText(String.valueOf(keyAbc_[resultAbc[19 + i]])
                                .toUpperCase(), startX2, startY + 2
                                * (BORDER_CENTER + qwertyH_), textPaint_);
                    }
                }

                /*
                 * 画字母键盘功能键的切换大小写
                 */
                // if (!isABC_) {
                // g.drawBitmap(keyBoard_shift_normal, BORDER, BORDER_TOP + 2
                // * BORDER_CENTER + 2 * qwertyH_, paint_);
                // } else {
                // g.drawBitmap(keyBoard_shift_down, BORDER, BORDER_TOP + 2
                // * BORDER_CENTER + 2 * qwertyH_, paint_);
                // }

                /*
                 * 画字母键盘功能键的切换大小写
                 */
                if (!isABC_) {
                    g.drawBitmap(keyBoard_shift_normal, BORDER + qwertyW_
                            - qwertyW_ / 4 + (qwertyW_ + colW_one) * 7,
                            BORDER_TOP + 2 * BORDER_CENTER + 2 * qwertyH_,
                            paint_);
                } else {
                    g.drawBitmap(keyBoard_shift_down, BORDER + qwertyW_
                            - qwertyW_ / 4 + (qwertyW_ + colW_one) * 7,
                            BORDER_TOP + 2 * BORDER_CENTER + 2 * qwertyH_,
                            paint_);
                }
                /*
                 * 画功能键的切换数字键
                 */
                g.drawBitmap(keyBoard_gray_enter, BORDER, BORDER_TOP
                        + (BORDER_CENTER + qwertyH_) * 3, paint_);

                // textPaint_.setColor(Color.WHITE);
                g.drawText(
                        "123",
                        BORDER
                                + (keyBoard_gray_enter.getWidth() - textPaint_
                                        .measureText("123")) / 2,
                        (qwertyH_ + BORDER_CENTER) * 3 + BORDER_TOP
                                + keyBoard_gray_enter.getHeight() / 2
                                + LPUtils.getScaledValue(8), textPaint_);

                /*
                 * 画logo
                 */
                g.drawBitmap(keyBoard_jsbank,
                        (width_ - keyBoard_jsbank.getWidth()) >> 1, BORDER_TOP
                                + 3 * BORDER_CENTER + 3 * qwertyH_, paint_);
                g.drawBitmap(
                        keyBoard_jsbank_logo,
                        (width_ - keyBoard_jsbank_logo.getWidth()) >> 1,
                        BORDER_TOP + 3 * BORDER_CENTER + 3 * qwertyH_
                                + LPUtils.getScaledValue(4), paint_);
                // g.drawText("江苏银行", (width_ + keyBoard_jsbank_logo.getWidth()
                // - textPaint_.measureText("江苏银行")) / 2,
                // (qwertyH_ + BORDER_CENTER) * 3 + BORDER_TOP +
                // keyBoard_gray_enter.getHeight() / 2 +
                // LPUtils.getScaledValue(5),
                // textPaint_);
                /*
                 * 画功能键的删除键
                 */
                textPaint_.setTextSize(PAINT_SIZE);
                if (isDel_) {
                    g.drawBitmap(keyBoard_del_down,
                            width_ - keyBoard_del_down.getWidth() - BORDER,
                            BORDER_TOP + 3 * BORDER_CENTER + 3 * qwertyH_,
                            paint_);
                    delEditAgainValue();
                } else {
                    g.drawBitmap(keyBoard_gray_del,
                            width_ - keyBoard_gray_del.getWidth() - BORDER,
                            BORDER_TOP + 3 * BORDER_CENTER + 3 * qwertyH_,
                            paint_);
                }
                // textPaint_.setColor(Color.WHITE);
                // g.drawText("确定", width_
                // - BORDER
                // - keyBoard_gray_enter.getWidth()
                // + (keyBoard_gray_enter.getWidth() - textPaint_
                // .measureText("确定")) / 2, BORDER_TOP + 3
                // * BORDER_CENTER + 3 * qwertyH_ + LPUtils.getScaledValue(25),
                // textPaint_);
            }

        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            // 按下去的横坐标
            int x = (int) event.getX();
            // 按下去的纵坐标
            int y = (int) event.getY();

            int rowFirst = BORDER_TOP + qwertyH_;
            int rowSecond = rowFirst + BORDER_CENTER + qwertyH_;
            int rowThird = rowSecond + BORDER_CENTER + qwertyH_;
            int rowFourth = rowThird + BORDER_CENTER + qwertyH_;
            // 触摸的方式
            int toucheEvent = event.getAction();
            switch (toucheEvent) {
                case MotionEvent.ACTION_DOWN:
                    String isWhat_ = null;
                    String isOut_ = null;
                    keyDownTime = System.currentTimeMillis();
                    if (isNum_) {
                        y = y - BORDER_TOP;
                        if (y >= 0 && y <= (numH_ + BORDER_NUMBER_CENTER) * 4) {
                            key_row = y / (numH_ + BORDER_NUMBER_CENTER);
                            key_column = x / (numW_ + BORDER_NUMBER_CENTER);
                        }
                    } else {

                        if (y > BORDER_TOP && y <= rowFirst) {
                            // 第一行
                            if (x > BORDER && x < width_) {
                                column1 = x / (qwertyW_ + colW_one);

                                if (isABC_) {
                                    // 大写字母
                                    isWhat_ = String.valueOf(
                                            keyAbc_[resultAbc[column1]])
                                            .toUpperCase();
                                } else {
                                    // 小写字母
                                    isWhat_ = String
                                            .valueOf(keyAbc_[resultAbc[column1]]);
                                }

                                if (column1 == 0) {
                                    isOut_ = "left";
                                } else if (column1 == 9) {
                                    isOut_ = "right";
                                }
                                // showPopWindow(BORDER + (column1) * (qwertyW_
                                // + colW_one) - (column1 == 0 ? qwertyW_ :
                                // windownShow_W),
                                // BORDER_TOP + qwertyH_ / 2, isWhat_,isOut_);
                            }
                        } else if (y > (rowFirst + BORDER_CENTER)
                                && y < rowSecond) {
                            // 第二行

                            if (x > BORDER_TWO && x < (width_ - BORDER_TWO)) {
                                column2 = (x - BORDER_TWO)
                                        / (qwertyW_ + colW_one);
                                if (isABC_) {
                                    isWhat_ = String.valueOf(
                                            keyAbc_[resultAbc[10
                                                    + (x - BORDER_TWO)
                                                    / (qwertyW_ + colW_one)]])
                                            .toUpperCase();
                                } else {
                                    isWhat_ = String
                                            .valueOf(keyAbc_[resultAbc[10
                                                    + (x - BORDER_TWO)
                                                    / (qwertyW_ + colW_one)]]);
                                }
                                // showPopWindow(BORDER_TWO + (column2) *
                                // (qwertyW_ + colW_one) - windownShow_W,
                                // BORDER_TOP + BORDER_CENTER + qwertyH_
                                // + qwertyH_ / 2, isWhat_,isOut_);
                            }

                        } else if (y > (rowSecond + BORDER_CENTER)
                                && y < rowThird) {
                            // 第三行
                            if (x > (BORDER + qwertyW_ - qwertyW_ / 4 + (qwertyW_ + colW_one) * 7)
                                    && x < (BORDER + qwertyW_ - qwertyW_ / 4
                                            + (qwertyW_ + colW_one) * 7 + keyBoard_shift_normal
                                                .getWidth())) {

                                isABC_ = !isABC_;

                            } else if (x > (BORDER + qwertyW_ - qwertyW_ / 4)
                                    && x < (BORDER + qwertyW_ - qwertyW_ / 4 + (qwertyW_ + colW_one) * 7)) {
                                column3 = (x - (BORDER + qwertyW_ - qwertyW_ / 4))
                                        / (qwertyW_ + colW_one);
                                if (isABC_) {
                                    isWhat_ = String.valueOf(
                                            keyAbc_[resultAbc[19 + column3]])
                                            .toUpperCase();
                                } else {
                                    isWhat_ = String
                                            .valueOf(keyAbc_[resultAbc[19 + column3]]);
                                }
                                // showPopWindow(
                                // column3
                                // * (qwertyW_ + colW_one)
                                // + (BORDER
                                // + qwertyW_ - qwertyW_ / 4) - windownShow_W
                                // , BORDER_TOP + qwertyH_ * 2
                                // + BORDER_CENTER * 2 + qwertyH_ / 2,
                                // isWhat_,isOut_);
                            }

                        } else if (y > (rowThird + BORDER_CENTER)
                                && y < rowFourth) {
                            // 第四行
                            if (x > BORDER
                                    && x < (BORDER + keyBoard_gray_enter
                                            .getWidth())) {
                                isNum_ = true;

                            } else if (x > (width_ - BORDER - keyBoard_gray_enter
                                    .getWidth()) && x < (width_ - BORDER)) {
                                isDel_ = true;
                                keyEnter_ = true;
                            }
                        }
                    }
                    invalidate();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    // if(Math.abs(x - 2) > 2 || Math.abs(y - 2) > 2 ){
                    // column1 = -1;
                    // column2 = -1;
                    // column3 = -1;
                    // column4 = -1;
                    // column5 = -1;
                    // }
                    invalidate();
                    return true;
                case MotionEvent.ACTION_UP:
                    if (isNum_) {
                        if (key_row != -1 && key_column != -1) {
                            isDelete_ = false;
                            if (key_row == 3) {
                                switch (key_column) {
                                    case 0:
                                        if (inputTypeNumber_) {
                                            keyEnter_ = false;
                                            dlg_.dismiss();
                                        } else {
                                            isDelete_ = true;
                                            isNum_ = false;
                                        }
                                        break;
                                    case 1:
                                        textBuffer_ = appendBuffer(
                                                String.valueOf(resultNum[9]),
                                                true);
                                        break;
                                    case 2:
                                        isDel_ = false;
                                        isDelete_ = true;
                                        if (null != textBuffer_
                                                && textBuffer_.length() > 0
                                                && tempEdit_.getText() != null
                                                && tempEdit_.getText()
                                                        .toString().trim()
                                                        .length() > 0) {
                                            try {
                                                int k = tempEdit_
                                                        .getSelectionEnd();
                                                textBuffer_ = textBuffer_
                                                        .deleteCharAt(k - 1);
                                            } catch (Exception e) {
                                            }
                                        }
                                        break;

                                    default:
                                        break;
                                }
                            } else {
                                int index = key_row * 3 + key_column;
                                if (index < resultNum.length) {
                                    textBuffer_ = appendBuffer(
                                            String.valueOf(resultNum[index]),
                                            true);

                                }
                            }

                            key_row = -1;
                            key_column = -1;
                        } else {
                            isDelete_ = true;
                        }
                    } else {
                        isDel_ = false;
                        keyEnter_ = false;
                        if (column1 != -1) {
                            // 第一行
                            if (isABC_) {
                                // 大写字母
                                textBuffer_ = appendBuffer(
                                        String.valueOf(
                                                keyAbc_[resultAbc[column1]])
                                                .toUpperCase(), false);
                            } else {
                                // 小写字母
                                textBuffer_ = appendBuffer(
                                        String.valueOf(keyAbc_[resultAbc[column1]]),
                                        false);
                            }
                        } else if (column2 != -1) {
                            // 第二行
                            if (isABC_) {
                                textBuffer_ = appendBuffer(
                                        String.valueOf(
                                                keyAbc_[resultAbc[10 + column2]])
                                                .toUpperCase(), false);
                            } else {
                                textBuffer_ = appendBuffer(
                                        String.valueOf(keyAbc_[resultAbc[10 + column2]]),
                                        false);
                            }

                        } else if (column3 != -1) {
                            // 第三行
                            if (isABC_) {
                                textBuffer_ = appendBuffer(
                                        String.valueOf(
                                                keyAbc_[resultAbc[19 + column3]])
                                                .toUpperCase(), false);
                            } else {
                                textBuffer_ = appendBuffer(
                                        String.valueOf(keyAbc_[resultAbc[19 + column3]]),
                                        false);
                            }
                        } else if (y > (rowThird + BORDER_CENTER)
                                && y < rowFourth) {
                            isDelete_ = true;
                            // 第四行
                            if (x > (width_ - BORDER - keyBoard_gray_del
                                    .getWidth()) && x < (width_ - BORDER)) {
                                isDel_ = false;
                                isDelete_ = true;
                                if (null != textBuffer_
                                        && textBuffer_.length() > 0
                                        && tempEdit_.getText() != null
                                        && tempEdit_.getText().toString()
                                                .trim().length() > 0) {
                                    try {
                                        int k = tempEdit_.getSelectionEnd();
                                        textBuffer_ = textBuffer_
                                                .deleteCharAt(k - 1);
                                    } catch (Exception e) {
                                    }
                                }
                                keyEnter_ = false;
                            }
                        } else if (y > rowFourth) {
                            isDelete_ = true;
                        }

                    }

                    // 每按一次键盘刷新一次
                    // resultAbc = getRandomNumber(26);
                    // resultNum = getRandomNumber(10);

                    column1 = -1;
                    column2 = -1;
                    column3 = -1;
                    column4 = -1;
                    column5 = -1;

                    tempEdit_.setText(getStringBuffer(textBuffer_, true)
                            .toString());
                    if (null != getStringBuffer(textBuffer_, true)) {
                        tempEdit_.setSelection(getStringBuffer(textBuffer_,
                                true).length());
                    }
                    // 用户输入的内容显示一秒后变为*号
                    new CountDownTimer(1000, 1000) {
                        public void onFinish() {

                            tempEdit_.setText(getStringBuffer(textBuffer_,
                                    false).toString());
                            if (null != getStringBuffer(textBuffer_, false)) {
                                tempEdit_.setSelection(getStringBuffer(
                                        textBuffer_, false).length());
                            }
                        }

                        @Override
                        public void onTick(long millisUntilFinished) {


                        }
                    }.start();

                    edit_.setText(textBuffer_);
                    if (null != textBuffer_) {
                        edit_.setSelection(textBuffer_.length());
                    }
                    dismissPopWindow();
                    invalidate();
                    vibrator_.vibrate(2 * VIBRATE_DURATION);
                    return true;
            }
            return false;
        }

        private int getIntX(int left, String str) {

            int startX = 0;
            startX = (int) (left + (qwertyW_ - textPaint_.measureText(str)) / 2);
            return startX;
        }

        private void showPopWindow(int eventX, int eventY, String isWhat,
                String isOut) {

            ShowView show = null;
            show = new ShowView(bv_, isWhat, isOut);
            LinearLayout ll = new LinearLayout(bv_);
            ll.addView(show);
            if (null != isOut) {
                if (isOut.equalsIgnoreCase("left")) {
                    eventX = eventX + LPUtils.getScaledValue(10);
                } else if (isOut.equalsIgnoreCase("right")) {
                    eventX = eventX + colW_one - LPUtils.getScaledValue(3);
                }
            }
            popWindow_ = new PopupWindow(ll,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            if (!popWindow_.isShowing()) {
                popWindow_.showAtLocation(this, Gravity.AXIS_X_SHIFT,
                        eventX + 1, eventY - LPUtils.getScaledValue(20));
            }
        }

        private void dismissPopWindow() {

            if (null != popWindow_ && popWindow_.isShowing()) {
                popWindow_.dismiss();
            }
        }

        // 得到一个拼接的buffer，用来显示点还是实际内容
        public StringBuffer getStringBuffer(StringBuffer text, boolean isShow) {

            if (text == null) {
                return text;
            }
            StringBuffer buf = new StringBuffer();
            if (isShow) {
                if (null != text && !"".equalsIgnoreCase(text.toString())) {
                    for (int i = 0; i <= text.length() - 2; i++) {
                        buf = buf.append("*");
                    }
                    if (isDelete_) {
                        buf = buf.append("*");
                    } else {
                        buf = buf.append(text.charAt(text.length() - 1));
                    }

                }
            } else {
                if (null != text) {
                    for (int i = 0; i <= text.length() - 1; i++) {
                        buf = buf.append("*");
                    }
                }
            }

            isDelete_ = false;
            return buf;
        }

        // 删除文本内容
        public void delEditValue(StringBuffer sb) {

            if (sb.length() == 0
                    && edit_.getText().toString().trim().length() > 0) {
                sb.append(edit_.getText().toString().trim());
            }
            if (sb.length() > 0) {
                sb = sb.deleteCharAt(sb.length() - 1);
                edit_.setText(sb);
                edit_.setSelection(edit_.getText().toString().trim().length());
            }

        }

        // 连删操作
        public void delEditAgainValue() {

            StringTicker.instance().notifyTicker();
            this.postInvalidate();
            if ((System.currentTimeMillis() - keyDownTime) / 100 > 1) {
                // 按下时间大于0.1秒 则执行连删输入框
                delEditValue(textBuffer_);
            }
        }

        public StringBuffer appendBuffer(String buf, boolean isAppend) {

            // 判断是否输入框有限制输入长度
            if (null != textBuffer_ && textBuffer_.length() >= maxSize) {
                isDelete_ = true;
                return textBuffer_;
            } else {
                if (inputTypeNumber_) {
                    if (isAppend) {
                        textBuffer_ = textBuffer_.append(buf);
                    } else {
                        isDelete_ = true;
                    }
                } else {
                    textBuffer_ = textBuffer_.append(buf);
                }
                return textBuffer_;
            }
        }

        public int[] getRandomNumber(int limit) {

            int[] result = new int[limit];
            for (int i = 0; i < limit; i++) {
                result[i] = i;
            }
            int w;
            Random rand = new Random();
            for (int i = limit - 1; i > 0; i--) {
                w = rand.nextInt(i);
                int t = result[i];
                result[i] = result[w];
                result[w] = t;
            }
            return result;
        }
    }

    /**
     * 确定按钮
     *
     * @author brookess
     */
    class buttonOk extends LinearLayout {

        TextView tv;
        boolean isSubmit_ = false;

        public buttonOk(Context context, boolean isSubmit) {

            super(context);
            isSubmit_ = isSubmit;
            tv = new TextView(context);
            tv.setTextSize(22);
            tv.setText("确定");
            // tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setTextColor(Color.WHITE);

            this.setBackgroundResource(R.drawable.keyboard_num_down);
            this.addView(tv);
            this.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            this.setGravity(Gravity.CENTER);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // this.setBackgroundResource(R.drawable.keyboard_abc_down);
                    break;
                case MotionEvent.ACTION_UP:
                    // this.setBackgroundResource(R.drawable.keyboard_abc_up);
                    dlg_.dismiss();
                    break;
                default:
                    // this.setBackgroundResource(R.drawable.keyboard_abc_up);
                    break;
            }
            return true;
        }

    }

    private class ShowView extends TextView {
        private Bitmap keyBoard_show_window;
        private Bitmap keyBoard_show_window_left;
        private Bitmap keyBoard_show_window_right;
        private int width_;
        private int height_;
        private TextPaint paint_;
        private String isWhat_;
        private String isOut_;

        public ShowView(Context context, String isWhat, String isOut) {

            super(context);
            isWhat_ = isWhat;
            isOut_ = isOut;
            init();
        }

        private void init() {
            paint_ = new TextPaint();
            paint_.setAntiAlias(true);
            paint_.setTextSize(LPUtils.getScaledValue(18));
            paint_.setTypeface(Typeface.DEFAULT_BOLD);
            paint_.setColor(Color.BLACK);

            keyBoard_show_window = BitmapFactory.decodeResource(getResources(),
                    R.drawable.keyboard_show_window);
            keyBoard_show_window_left = BitmapFactory.decodeResource(
                    getResources(), R.drawable.keyboard_show_window_left);
            keyBoard_show_window_right = BitmapFactory.decodeResource(
                    getResources(), R.drawable.keyboard_show_window_right);

            int w = LPUtils.screenWidth_ / 8;
            int h = w * keyBoard_show_window.getHeight()
                    / keyBoard_show_window.getWidth();
            keyBoard_show_window = Bitmap.createScaledBitmap(
                    keyBoard_show_window, w, h, true);
            keyBoard_show_window_left = Bitmap.createScaledBitmap(
                    keyBoard_show_window_left, w, h, true);
            keyBoard_show_window_right = Bitmap.createScaledBitmap(
                    keyBoard_show_window_right, w, h, true);

            if (null != isOut_) {
                width_ = keyBoard_show_window_left.getWidth();
            } else {
                width_ = keyBoard_show_window.getWidth();
            }
            height_ = keyBoard_show_window.getHeight();
            LayoutParams lp = (LayoutParams) this
                    .getLayoutParams();

            if (lp == null) {
                setLayoutParams(new LayoutParams(width_, height_));
            } else {
                lp.height = height_;
                lp.width = width_;
                setLayoutParams(lp);
            }
        }

        public void onDraw(Canvas g) {

            if (null != isOut_) {
                if (isOut_.equalsIgnoreCase("left")) {
                    g.drawBitmap(keyBoard_show_window_left, 0, 0, paint_);
                } else if (isOut_.equalsIgnoreCase("right")) {
                    g.drawBitmap(keyBoard_show_window_right, 0, 0, paint_);
                }
            } else {
                g.drawBitmap(keyBoard_show_window, 0, 0, paint_);
            }
            if (null != isWhat_) {
                g.drawText(isWhat_, (width_ - paint_.measureText(isWhat_)) / 2,
                        height_ / 2 - LPUtils.getScaledValue(8), paint_);
            }
        }
    }
}
