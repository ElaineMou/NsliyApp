package com.elaine.nsliyapplication.input;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.elaine.nsliyapplication.R;

import java.util.ArrayList;

/**
 * Created by Elaine on 12/26/2014.
 */
public class Pronunciation {

    public static final String[] SYLLABLES = new String[] {"ai","an","ang","ao","ba","bai","ban",
            "bang","bao","bei","ben","beng","bi","bian","biao","bie","bin","bing","bo","bu","ca",
            "cai","can","cang","cao","ce","cen","ceng","cha","chai","chan","chang","chao","che",
            "chen","cheng","chi","chong","chou","chu","chuai","chuan","chuang","chui","chun","chuo",
            "ci","cong","cou","cu","cuan","cui","cun","cuo","da","dai","dan","dang","dao","de",
            "deng","di","dian","diao","die","ding","diu","dong","dou","du","duan","dui","dun","duo",
            "e","en","er","fa","fan","fang","fei","fen","feng","fo","fou","fu","ga","gai","gan",
            "gang","gao","ge","gei","gen","geng","gong","gou","gu","gua","guai","guan","guang",
            "gui","gun","guo","ha","hai","han","hang","hao","he","hei","hen","heng","hong","hou",
            "hu","hua","huai","huan","huang","hui","hun","huo","ji","jia","jian","jiang","jiao",
            "jie","jin","jing","jiong","jiu","ju","juan","jue","jun","ka","kai","kan","kang","kao",
            "ke","ken","keng","kong","kou","ku","kua","kuai","kuan","kuang","kui","kun","kuo","la",
            "lai","lan","lang","lao","le","lei","leng","li","lia","lian","liang","liao","lie","lin",
            "ling","liu","lo","long","lou","lu","luan","lue","lun","luo","lv","ma","mai","man",
            "mang","mao","me","mei","men","meng","mi","mian","miao","mie","min","ming","miu","mo",
            "mou","mu","na","nai","nan","nang","nao","ne","nei","nen","neng","ni","nian","niang",
            "niao","nie","nin","ning","niu","nong","nu","nuan","nue","nuo","nv","o","on","ou","pa",
            "pai","pan","pang","pao","pei","pen","peng","pi","pian","piao","pie","pin","ping","po",
            "pou","pu","qi","qia","qian","qiang","qiao","qie","qin","qing","qiong","qiu","qu",
            "quan","que","qun","ran","rang","rao","re","ren","reng","ri","rong","rou","ru","ruan",
            "rui","run","ruo","sa","sai","san","sang","sao","se","seng","sha","shai","shan","shang",
            "shao","she","shei","shen","sheng","shi","shu","shua","shuai","shuan","shuang","shui",
            "shun","shuo","si","song","sou","su","suan","sui","sun","suo","ta","tai","tan","tang",
            "tao","te","teng","ti","tian","tiao","tie","ting","tong","tou","tu","tuan","tui","tun",
            "tuo","wa","wai","wan","wang","wei","wen","weng","wo","wu","xi","xia","xian","xiang",
            "xiao","xie","xin","xing","xiong","xiu","xu","xuan","xue","xun","ya","yan","yang","yao",
            "ye","yi","yin","ying","yong","you","yu","yuan","yue","yun","za","zai","zan","zang",
            "zao","ze","zei","zen","zeng","zha","zhai","zhan","zhang","zhao","zhe","zhen","zheng",
            "zhi","zhong","zhou","zhu","zhua","zhuan","zhuang","zhui","zhun","zhuo","zi","zong",
            "zou","zu","zuan","zui","zun","zuo"};

    public static enum Tone {FIRST("1"),SECOND("2"),THIRD("3"),FOURTH("4"),NEUTRAL(" "),UNKNOWN("?");

        Tone(String name){
            appearance = name;
        }

        String appearance;

        @Override
        public String toString(){
            return appearance;
        }
    };

    public final String syllable;
    public final Tone tone;

    public Pronunciation(String syllable, Tone tone){
        this.syllable = syllable;
        this.tone = tone;
    }

    /**
     * Created by Elaine on 12/26/2014.
     */
    public static class PronunciationAdapter extends BaseAdapter {

        private final ArrayList<Pronunciation> pronunciations;
        private final Context context;

        public PronunciationAdapter(Context context, ArrayList<Pronunciation> pronunciations){
            this.pronunciations = pronunciations;
            this.context = context;
        }

        @Override
        public int getCount() {
            return pronunciations.size();
        }

        @Override
        public Object getItem(int position) {
            return pronunciations.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            PronunciationView pronunciationView = new PronunciationView(context);
            pronunciationView.setSyllable(pronunciations.get(position).syllable);
            pronunciationView.setTone(pronunciations.get(position).tone);

            pronunciationView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pronunciations.remove(position);
                    notifyDataSetChanged();
                }
            });
            return pronunciationView;
        }
    }
}