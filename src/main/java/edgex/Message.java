package edgex;

/**
 * Class description goes here.
 *
 * @author 陈永佳 (yoojiachen@gmail.com)
 * @version 0.0.1
 */
public interface Message {
    byte[] bytes();

    int size();

    ////

    final class Impl implements Message {

        private final byte[] frames;

        Impl(byte[] frames) {
            this.frames = frames;
        }

        @Override
        public byte[] bytes() {
            return frames;
        }

        @Override
        public int size() {
            return frames.length;
        }
    }

    ////

    static Message newString(String data) {
        return new Impl(data.getBytes());
    }

    static Message newBytes(byte[] data) {
        return new Impl(data);
    }
}
