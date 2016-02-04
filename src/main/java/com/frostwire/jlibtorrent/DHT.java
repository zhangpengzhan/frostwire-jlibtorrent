package com.frostwire.jlibtorrent;

import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.DhtGetPeersReplyAlert;
import com.frostwire.jlibtorrent.alerts.DhtImmutableItemAlert;
import com.frostwire.jlibtorrent.swig.libtorrent;
import com.frostwire.jlibtorrent.swig.settings_pack;
import com.frostwire.jlibtorrent.swig.sha1_hash;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.frostwire.jlibtorrent.alerts.AlertType.DHT_GET_PEERS_REPLY;
import static com.frostwire.jlibtorrent.alerts.AlertType.DHT_IMMUTABLE_ITEM;

/**
 * @author gubatron
 * @author aldenml
 */
public final class DHT {

    private static final int[] DHT_IMMUTABLE_ITEM_TYPES = {DHT_IMMUTABLE_ITEM.getSwig()};
    private static final int[] DHT_GET_PEERS_REPLY_ALERT_TYPES = {DHT_GET_PEERS_REPLY.getSwig()};

    private final Session s;

    public DHT(Session s) {
        this.s = s;
    }

    public void start() {
        toggleDHT(true);
    }

    public void stop() {
        toggleDHT(false);
    }

    public boolean running() {
        return s.isDHTRunning();
    }

    public Entry get(Sha1Hash sha1, long timeout) {
        final Sha1Hash target = sha1;
        final Entry[] result = {null};
        final CountDownLatch signal = new CountDownLatch(1);

        AlertListener l = new AlertListener() {

            @Override
            public int[] types() {
                return DHT_IMMUTABLE_ITEM_TYPES;
            }

            @Override
            public void alert(Alert<?> alert) {
                if (alert instanceof DhtImmutableItemAlert) {
                    DhtImmutableItemAlert itemAlert = (DhtImmutableItemAlert) alert;
                    if (target.equals(itemAlert.getTarget())) {
                        result[0] = itemAlert.getItem();
                        signal.countDown();
                    }
                }
            }
        };

        s.addListener(l);

        s.dhtGetItem(target);

        try {
            signal.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore
        }

        s.removeListener(l);

        return result[0];
    }

    public Sha1Hash put(Entry entry) {
        return s.dhtPutItem(entry);
    }

    public ArrayList<TcpEndpoint> getPeers(Sha1Hash sha1, long timeout) {
        final Sha1Hash target = sha1;
        final Object[] result = {new ArrayList<TcpEndpoint>()};
        final CountDownLatch signal = new CountDownLatch(1);

        AlertListener l = new AlertListener() {

            @Override
            public int[] types() {
                return DHT_GET_PEERS_REPLY_ALERT_TYPES;
            }

            @Override
            public void alert(Alert<?> alert) {
                if (alert instanceof DhtGetPeersReplyAlert) {
                    DhtGetPeersReplyAlert replyAlert = (DhtGetPeersReplyAlert) alert;
                    if (target.equals(replyAlert.infoHash())) {
                        result[0] = replyAlert.peers();
                        signal.countDown();
                    }
                }
            }
        };

        s.addListener(l);

        s.dhtGetPeers(target);

        try {
            signal.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore
        }

        s.removeListener(l);

        return (ArrayList<TcpEndpoint>) result[0];
    }

    public void announce(Sha1Hash sha1, int port, int flags) {
        s.dhtAnnounce(sha1, port, flags);
    }

    public void announce(Sha1Hash sha1) {
        s.dhtAnnounce(sha1);
    }

    public static byte[][] createKeypair() {
        byte[] seed = new byte[Ed25519.SEED_SIZE];
        Ed25519.createSeed(seed);

        byte[] publicKey = new byte[Ed25519.PUBLIC_KEY_SIZE];
        byte[] privateKey = new byte[Ed25519.PRIVATE_KEY_SIZE];

        Ed25519.createKeypair(publicKey, privateKey, seed);

        byte[][] keys = new byte[2][];
        keys[0] = publicKey;
        keys[1] = privateKey;

        return keys;
    }

    private void toggleDHT(boolean on) {
        SettingsPack pack = new SettingsPack();
        pack.setBoolean(settings_pack.bool_types.enable_dht.swigValue(), on);
        s.applySettings(pack);
    }

    /**
     * calculate the target hash for an immutable item.
     *
     * @param e
     * @return
     */
    public static Sha1Hash itemTargetId(Entry e) {
        return null;//new Sha1Hash(dht_item.item_target_id(e.getSwig().bencode()));
    }

    /**
     * calculate the target hash for a mutable item.
     *
     * @param salt
     * @param pk
     * @return
     */
    public static Sha1Hash itemTargetId(byte[] salt, byte[] pk) {
        sha1_hash h = libtorrent.dht_item_target_id(Vectors.bytes2byte_vector(salt), Vectors.bytes2byte_vector(pk));
        return new Sha1Hash(h);
    }

    /**
     * Given a byte range ``v`` and an optional byte range ``salt``, a
     * sequence number, public key ``pk`` (must be 32 bytes) and a secret key
     * ``sk`` (must be 64 bytes), this function produces a signature which
     * is written into a 64 byte buffer pointed to by ``sig``. The caller
     * is responsible for allocating the destination buffer that's passed in
     * as the ``sig`` argument. Typically it would be allocated on the stack.
     *
     * @param e
     * @param salt
     * @param seq
     * @param pk
     * @param sk
     * @param sig
     */
    public static void signMutableItem(Entry e, String salt, int seq, byte[] pk, byte[] sk, byte[] sig) {
        /*
        if (sig == null || sig.length != Ed25519.SIGNATURE_SIZE) {
            throw new IllegalArgumentException("The signature array must be a valid one with length " + Ed25519.SIGNATURE_SIZE);
        }

        char_vector sig_v = Vectors.new_char_vector(Ed25519.SIGNATURE_SIZE);

        dht_item.sign_mutable_item(e.getSwig().bencode(), salt, seq,
                Vectors.bytes2char_vector(pk), Vectors.bytes2char_vector(sk), sig_v);

        Vectors.char_vector2bytes(sig_v, sig);
        */
    }

    public static boolean verifyMutableItem(Entry e, String salt, int seq, byte[] pk, byte[] sig) {
        return false;/*dht_item.verify_mutable_item(e.getSwig().bencode(), salt, seq,
                Vectors.bytes2char_vector(pk),
                Vectors.bytes2char_vector(sig));*/
    }

    public static int canonicalString(Entry e, int seq, String salt, byte[] out) {
       /* if (out == null || out.length != 1200) {
            throw new IllegalArgumentException("The out array must be a valid one with length 1200");
        }

        char_vector out_v = Vectors.new_char_vector(1200);
        int r = dht_item.canonical_string(e.getSwig().bencode(),
                seq, salt, out_v);

        Vectors.char_vector2bytes(out_v, out);

        return r;*/
        return 0;
    }
}
