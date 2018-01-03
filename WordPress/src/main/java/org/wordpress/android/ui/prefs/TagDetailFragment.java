package org.wordpress.android.ui.prefs;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.fluxc.model.TermModel;
import org.wordpress.android.fluxc.store.TaxonomyStore;
import org.wordpress.android.util.EditTextUtils;
import org.wordpress.android.util.StringUtils;

import static org.wordpress.android.ui.reader.utils.ReaderUtils.sanitizeWithDashes;

/**
 * A fragment for editing a tag
 */
public class TagDetailFragment extends Fragment {
    private static final String ARGS_TERM = "term";
    private static final String ARGS_IS_NEW_TERM = "is_new";

    static final String TAG = "TagDetailFragment";

    public interface OnTagDetailListener {
        void onRequestDeleteTag(@NonNull TermModel tag);
    }

    private EditText mNameView;
    private EditText mDescriptionView;

    private TermModel mTerm;
    private boolean mIsNewTerm;
    private OnTagDetailListener mListener;

    /*
     * pass an existing term to edit it, or pass null to create a new one
     */
    public static TagDetailFragment newInstance(@Nullable TermModel term) {
        TagDetailFragment fragment = new TagDetailFragment();
        Bundle args = new Bundle();
        if (term == null) {
            args.putBoolean(ARGS_IS_NEW_TERM, true);
            term = new TermModel();
            term.setName("");
            term.setDescription("");
            term.setTaxonomy(TaxonomyStore.DEFAULT_TAXONOMY_TAG);
        }
        args.putSerializable(ARGS_TERM, term);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WordPress) getActivity().getApplication()).component().inject(this);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.tag_detail_fragment, container, false);

        mNameView = view.findViewById(R.id.edit_name);
        mDescriptionView = view.findViewById(R.id.edit_description);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTerm = (TermModel) getArguments().getSerializable(ARGS_TERM);
        mIsNewTerm = getArguments().getBoolean(ARGS_IS_NEW_TERM);

        loadTagDetail();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tag_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_trash).setVisible(!mIsNewTerm);
        menu.findItem(R.id.menu_search).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.menu_trash && mListener != null) {
            mListener.onRequestDeleteTag(mTerm);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setOnTagDetailListener(@NonNull OnTagDetailListener listener) {
        mListener = listener;
    }

    private void loadTagDetail() {
        if (!isAdded()) return;

        if (mIsNewTerm) {
            getActivity().setTitle(R.string.add_new_tag);
        } else {
            getActivity().setTitle(mTerm.getName());
        }

        mNameView.setText(mTerm.getName());
        mDescriptionView.setText(mTerm.getDescription());

        mNameView.requestFocus();
        mNameView.setSelection(mNameView.getText().length());
    }

    boolean hasChanges() {
        return !TextUtils.isEmpty(mTerm.getName())
                && (!StringUtils.equals(mTerm.getName(), EditTextUtils.getText(mNameView))
                || !StringUtils.equals(mTerm.getDescription(), EditTextUtils.getText(mDescriptionView)));
    }

    @NonNull TermModel getTerm() {
        String thisName = EditTextUtils.getText(mNameView);
        String thisDescription = EditTextUtils.getText(mDescriptionView);
        mTerm.setName(thisName);
        mTerm.setDescription(thisDescription);
        if (mIsNewTerm) {
            mTerm.setSlug(sanitizeWithDashes(thisName));
        }
        return mTerm;
    }

    boolean isNewTerm() {
        return mIsNewTerm;
    }
}
