
public class Hash {
    public static class Info {
        public long mKey;
        public int mLower;
        public int mUpper;
        public int mDepth;

        public Info() {
        }

        public Info(Info info) {
            mKey = info.mKey;
            mLower = info.mLower;
            mUpper = info.mUpper;
            mDepth = info.mDepth;
        }

        public Info(long key, int lower, int upper, int depth) {
            mKey = key;
            mLower = lower;
            mUpper = upper;
            mDepth = depth;
        }

        public void copy(Info info) {
            mKey = info.mKey;
            mLower = info.mLower;
            mUpper = info.mUpper;
            mDepth = info.mDepth;
        }
    }

    private int mCount;
    private long mMask;
    private Info[] mInfo;

    public Hash(int size) {
        mCount = 1 << size;
        mMask = mCount - 1;
        mInfo = new Info[mCount];
        for (int i = 0; i < mCount; i++) {
            mInfo[i] = new Info();
        }
    }

    public void clear() {
        for (int i = 0; i < mCount; i++) {
            mInfo[i].mKey = (long)~i;
        }
    }

    public boolean set(Info info) {
        int id = (int)(info.mKey & mMask);
        mInfo[id].copy(info);
        return true;
    }

    public Info get(long key) {
        int id = (int)(key & mMask);
        if (mInfo[id].mKey == key) {
            return new Info(mInfo[id]);
        }
        return null;
    }
}
