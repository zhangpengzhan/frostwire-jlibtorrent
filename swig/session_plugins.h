#include "libtorrent/buffer.hpp"

struct swig_torrent_plugin;
struct swig_peer_plugin;

struct swig_plugin : plugin {

    virtual ~swig_plugin() {
    }

    boost::shared_ptr<torrent_plugin> new_torrent(torrent*, void*);

    virtual swig_torrent_plugin* new_torrent2(libtorrent::torrent_handle th);
/*
    virtual void on_alert(alert const*) {}

    virtual bool on_unknown_torrent(sha1_hash const& info_hash, peer_connection* pc, add_torrent_params& p) {
        return false;
    }
*/
    virtual void on_tick() {
    }
/*
    virtual bool on_optimistic_unchoke(std::vector<torrent_peer*>& peers) {
        return false;
    }

    virtual void save_state(entry&) const {
    }

    virtual void load_state(bdecode_node const&) {
    }*/
};

struct swig_torrent_plugin: torrent_plugin
{
    swig_torrent_plugin() {
    }

    virtual ~swig_torrent_plugin() {
    }

    boost::shared_ptr<peer_plugin> new_connection(peer_connection* pc);

    virtual swig_peer_plugin* new_connection2(libtorrent::peer_connection *pc);

    virtual void on_piece_pass(int index) {
    }

    virtual void on_piece_failed(int index) {
    }

    virtual void tick() {
    }

    virtual bool on_pause() {
        return false;
    }

    virtual bool on_resume() {
        return false;
    }

    virtual void on_files_checked() {
    }

    virtual void on_state(int s) {
    }

    virtual void on_unload() {
    }

    virtual void on_load() {
    }
/*
    virtual void on_add_peer(tcp::endpoint const&, int src, int flags) {
    }*/
};

struct swig_peer_plugin : peer_plugin
{
    swig_peer_plugin() {
    }

    virtual ~swig_peer_plugin() {
    }

    virtual char const* type() const {
        return "swig";
    }
/*
    virtual void add_handshake(entry& h) {
    }

    virtual void on_disconnect(error_code const& ec) {}
*/
    virtual void on_connected() {}

//    virtual bool on_handshake(char const* reserved_bits) { return true; }
/*
    virtual bool on_extension_handshake(bdecode_node const& h) {
        return true;
    }*/

    virtual bool on_choke() { return false; }
    virtual bool on_unchoke() { return false; }
    virtual bool on_interested() { return false; }
    virtual bool on_not_interested() { return false; }
    virtual bool on_have(int index) { return false; }
    virtual bool on_dont_have(int index) { return false; }
    //virtual bool on_bitfield(bitfield const& bitfield) { return false; }
    virtual bool on_have_all() { return false; }
    virtual bool on_have_none() { return false; }
    virtual bool on_allowed_fast(int index) { return false; }

    /*
    virtual bool on_request(peer_request const&) { return false; }
    virtual bool on_piece(peer_request const& piece, disk_buffer_holder& data) {
        return false;
    }
    virtual bool on_cancel(peer_request const&) { return false; }
    virtual bool on_reject(peer_request const&) { return false; }
    */
    virtual bool on_suggest(int index) { return false; }

    virtual void sent_unchoke() {}

    virtual void sent_payload(int bytes) {}

    //virtual bool can_disconnect(error_code const& ec) { return true; }
/*
    virtual bool on_extended(int length, int msg, buffer::const_interval body)
    {
        return true;
    }*/
/*
    virtual bool on_unknown_message(int length, int msg,
    			buffer::const_interval body)
    { return false; }
*/
    virtual void on_piece_pass(int index) {}
    virtual void on_piece_failed(int index) {}

    virtual void tick() {}

    //virtual bool write_request(peer_request const&) { return false; }
};

boost::shared_ptr<torrent_plugin> swig_plugin::new_torrent(torrent* t, void*) {
    return boost::shared_ptr<torrent_plugin>(new_torrent2(t->get_handle()));
}

swig_torrent_plugin* swig_plugin::new_torrent2(torrent_handle th) {
    return new swig_torrent_plugin();
}

boost::shared_ptr<peer_plugin> swig_torrent_plugin::new_connection(peer_connection* pc) {
    return boost::shared_ptr<peer_plugin>(new_connection2(pc));
}

swig_peer_plugin* swig_torrent_plugin::new_connection2(peer_connection *pc) {
    return new swig_peer_plugin();
}