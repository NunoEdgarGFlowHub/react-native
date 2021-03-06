/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

// switchview because switch is a keyword
package com.facebook.react.views.switchviewview;

import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.facebook.csslayout.CSSNode;
import com.facebook.csslayout.MeasureOutput;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.CatalystStylesDiffMap;
import com.facebook.react.uimanager.ReactShadowNode;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIProp;
import com.facebook.react.uimanager.ViewProps;

/**
 * View manager for {@link ReactSwitch} components.
 */
public class ReactSwitchManager extends SimpleViewManager<ReactSwitch> {

  private static final String REACT_CLASS = "AndroidSwitch";
  @UIProp(UIProp.Type.BOOLEAN) public static final String PROP_ENABLED = ViewProps.ENABLED;
  @UIProp(UIProp.Type.BOOLEAN) public static final String PROP_ON = ViewProps.ON;

  private static class ReactSwitchShadowNode extends ReactShadowNode implements
      CSSNode.MeasureFunction {

    private int mWidth;
    private int mHeight;
    private boolean mMeasured;

    private ReactSwitchShadowNode() {
      setMeasureFunction(this);
    }

    @Override
    public void measure(CSSNode node, float width, MeasureOutput measureOutput) {
      if (!mMeasured) {
        // Create a switch with the default config and measure it; since we don't (currently)
        // support setting custom switch text, this is fine, as all switches will measure the same
        // on a specific device/theme/locale combination.
        ReactSwitch reactSwitch = new ReactSwitch(getThemedContext());
        final int spec = View.MeasureSpec.makeMeasureSpec(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            View.MeasureSpec.UNSPECIFIED);
        reactSwitch.measure(spec, spec);
        mWidth = reactSwitch.getMeasuredWidth();
        mHeight = reactSwitch.getMeasuredHeight();
        mMeasured = true;
      }
      measureOutput.width = mWidth;
      measureOutput.height = mHeight;
    }
  }

  private static final CompoundButton.OnCheckedChangeListener ON_CHECKED_CHANGE_LISTENER =
      new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          ReactContext reactContext = (ReactContext) buttonView.getContext();
          reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher().dispatchEvent(
              new ReactSwitchEvent(
                  buttonView.getId(),
                  SystemClock.uptimeMillis(),
                  isChecked));
        }
      };

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  public ReactShadowNode createCSSNodeInstance() {
    return new ReactSwitchShadowNode();
  }

  @Override
  protected ReactSwitch createViewInstance(ThemedReactContext context) {
    ReactSwitch view = new ReactSwitch(context);
    view.setShowText(false);
    return view;
  }

  @Override
  public void updateView(ReactSwitch view, CatalystStylesDiffMap props) {
    super.updateView(view, props);
    if (props.hasKey(PROP_ENABLED)) {
      view.setEnabled(props.getBoolean(PROP_ENABLED, true));
    }
    if (props.hasKey(PROP_ON)) {
      // we set the checked change listener to null and then restore it so that we don't fire an
      // onChange event to JS when JS itself is updating the value of the switch
      view.setOnCheckedChangeListener(null);
      view.setOn(props.getBoolean(PROP_ON, false));
      view.setOnCheckedChangeListener(ON_CHECKED_CHANGE_LISTENER);
    }
  }

  @Override
  protected void addEventEmitters(final ThemedReactContext reactContext, final ReactSwitch view) {
    view.setOnCheckedChangeListener(ON_CHECKED_CHANGE_LISTENER);
  }
}
