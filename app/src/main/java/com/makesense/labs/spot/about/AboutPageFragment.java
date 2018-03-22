package com.makesense.labs.spot.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makesense.labs.spot.R;

/**
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 * @description Fragment displays app summary, version number and developer information
 */
public class AboutPageFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about_page, container, false);

        TextView mEmailTextView = rootView.findViewById(R.id.emailIdTextView);
        TextView mGitHubTextView = rootView.findViewById(R.id.gitLinkTextView);

        mEmailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL,
                        new String[]{getString(R.string.developer_email_id)});
                intent.setType("*/*");
                intent.setPackage("com.google.android.gm");
                startActivity(Intent.createChooser(intent, getString(R.string.send_mail_title)));
            }
        });

        mGitHubTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url))));
            }
        });

        return rootView;
    }
}
