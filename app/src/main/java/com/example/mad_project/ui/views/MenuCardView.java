package com.example.mad_project.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mad_project.R;
import com.google.android.material.card.MaterialCardView;

public class MenuCardView extends MaterialCardView {
    private ImageView iconView;
    private TextView titleView;
    private TextView subtitleView;

    public MenuCardView(Context context) {
        super(context);
        init(context, null);
    }

    public MenuCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MenuCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.layout_menu_card, this);

        iconView = findViewById(R.id.card_icon);
        titleView = findViewById(R.id.card_title);
        subtitleView = findViewById(R.id.card_subtitle);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MenuCard);
            try {
                int iconRes = a.getResourceId(R.styleable.MenuCard_cardIcon, 0);
                String title = a.getString(R.styleable.MenuCard_cardTitle);
                String subtitle = a.getString(R.styleable.MenuCard_cardSubtitle);

                if (iconRes != 0) {
                    iconView.setImageResource(iconRes);
                }
                if (title != null) {
                    titleView.setText(title);
                }
                if (subtitle != null) {
                    subtitleView.setText(subtitle);
                }
            } finally {
                a.recycle();
            }
        }
    }

    // Optional: Add setter methods for programmatic updates
    public void setCardIcon(int resourceId) {
        iconView.setImageResource(resourceId);
    }

    public void setCardTitle(String title) {
        titleView.setText(title);
    }

    public void setCardSubtitle(String subtitle) {
        subtitleView.setText(subtitle);
    }
}