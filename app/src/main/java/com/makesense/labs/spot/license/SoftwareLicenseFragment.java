package com.makesense.labs.spot.license;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;

import com.makesense.labs.spot.R;


/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 * @description Fragment displays license information of all third party libraries using in app
 */
public class SoftwareLicenseFragment extends Fragment {

    private String TAG = "SoftwareLicenseFragment";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "TamperMonitoring, SoftwareLicenseFragment, onCreateView()");
        //Inflate the layout for this fragment
        View rootView = inflater.inflate(
                R.layout.fragment_license, container, false);

        WebView thirdPartyLicenseWebView = (WebView) rootView.findViewById(R.id.thirdPartyLicense);

        // Loads licenses.html page into WebView
        thirdPartyLicenseWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        thirdPartyLicenseWebView.setWebViewClient(new WebViewClient());
        thirdPartyLicenseWebView.loadUrl("file:///android_asset/licenses.html");

        return rootView;
    }

    private class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}