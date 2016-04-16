package top.leixiao.mindnote.catogery;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;

public class CatogetyCustomActivity extends AppCompatActivity implements OnClickListener {


    @Override
    public void onClick(View v) {

    }

    public static class CatogeryHolder {
        public String mName;
        public int mId;

        public CatogeryHolder(int id, String name) {
            this.mId = id;
            this.mName = name;
        }
    }


}
