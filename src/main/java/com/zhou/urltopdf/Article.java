package com.zhou.urltopdf;

import java.util.List;

public class Article {
    private String aid;
    private String title;
    private String cover;
    private String link;
    private String digest;
    private long update_time;
    private long appmsgid;
    private int itemidx;
    private int item_show_type;
    private String author_name;
    private List<?> tagid;
    private long create_time;
    private int is_pay_subscribe;
    private int has_red_packet_cover;
    private String album_id;
    private int checking;
    private String media_duration;
    private int mediaapi_publish_status;
    private int copyright_type;
    private List<?> appmsg_album_infos;
    private PayAlbumInfo pay_album_info;
    private boolean is_deleted;
    private int ban_flag;
    private String pic_cdn_url_235_1;
    private String pic_cdn_url_16_9;
    private String pic_cdn_url_3_4;
    private String pic_cdn_url_1_1;
    private String cover_img;
    private CoverImgThemeColor cover_img_theme_color;
    private LineInfo line_info;
    private int copyright_stat;
    private int is_rumor_refutation;
    private int multi_picture_cover;
    private List<?> share_imageinfo;
    private String fakeid;

    // Getters and Setters
    public static class PayAlbumInfo {
        private List<?> appmsg_album_infos;

        public List<?> getAppmsg_album_infos() {
            return appmsg_album_infos;
        }

        public void setAppmsg_album_infos(List<?> appmsg_album_infos) {
            this.appmsg_album_infos = appmsg_album_infos;
        }
    }

    public static class CoverImgThemeColor {
        private int r;
        private int g;
        private int b;

        public int getR() {
            return r;
        }

        public void setR(int r) {
            this.r = r;
        }

        public int getG() {
            return g;
        }

        public void setG(int g) {
            this.g = g;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }
    }

    public static class LineInfo {
        private int use_line;
        private int line_count;
        private int is_appmsg_flag;
        private int is_use_flag;

        public int getUse_line() {
            return use_line;
        }

        public void setUse_line(int use_line) {
            this.use_line = use_line;
        }

        public int getLine_count() {
            return line_count;
        }

        public void setLine_count(int line_count) {
            this.line_count = line_count;
        }

        public int getIs_appmsg_flag() {
            return is_appmsg_flag;
        }

        public void setIs_appmsg_flag(int is_appmsg_flag) {
            this.is_appmsg_flag = is_appmsg_flag;
        }

        public int getIs_use_flag() {
            return is_use_flag;
        }

        public void setIs_use_flag(int is_use_flag) {
            this.is_use_flag = is_use_flag;
        }
    }

    // Getters and Setters for all fields
    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public long getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(long update_time) {
        this.update_time = update_time;
    }

    public long getAppmsgid() {
        return appmsgid;
    }

    public void setAppmsgid(long appmsgid) {
        this.appmsgid = appmsgid;
    }

    public int getItemidx() {
        return itemidx;
    }

    public void setItemidx(int itemidx) {
        this.itemidx = itemidx;
    }

    public int getItem_show_type() {
        return item_show_type;
    }

    public void setItem_show_type(int item_show_type) {
        this.item_show_type = item_show_type;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public List<?> getTagid() {
        return tagid;
    }

    public void setTagid(List<?> tagid) {
        this.tagid = tagid;
    }

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public int getIs_pay_subscribe() {
        return is_pay_subscribe;
    }

    public void setIs_pay_subscribe(int is_pay_subscribe) {
        this.is_pay_subscribe = is_pay_subscribe;
    }

    public int getHas_red_packet_cover() {
        return has_red_packet_cover;
    }

    public void setHas_red_packet_cover(int has_red_packet_cover) {
        this.has_red_packet_cover = has_red_packet_cover;
    }

    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public int getChecking() {
        return checking;
    }

    public void setChecking(int checking) {
        this.checking = checking;
    }

    public String getMedia_duration() {
        return media_duration;
    }

    public void setMedia_duration(String media_duration) {
        this.media_duration = media_duration;
    }

    public int getMediaapi_publish_status() {
        return mediaapi_publish_status;
    }

    public void setMediaapi_publish_status(int mediaapi_publish_status) {
        this.mediaapi_publish_status = mediaapi_publish_status;
    }

    public int getCopyright_type() {
        return copyright_type;
    }

    public void setCopyright_type(int copyright_type) {
        this.copyright_type = copyright_type;
    }

    public List<?> getAppmsg_album_infos() {
        return appmsg_album_infos;
    }

    public void setAppmsg_album_infos(List<?> appmsg_album_infos) {
        this.appmsg_album_infos = appmsg_album_infos;
    }

    public PayAlbumInfo getPay_album_info() {
        return pay_album_info;
    }

    public void setPay_album_info(PayAlbumInfo pay_album_info) {
        this.pay_album_info = pay_album_info;
    }

    public boolean isIs_deleted() {
        return is_deleted;
    }

    public void setIs_deleted(boolean is_deleted) {
        this.is_deleted = is_deleted;
    }

    public int getBan_flag() {
        return ban_flag;
    }

    public void setBan_flag(int ban_flag) {
        this.ban_flag = ban_flag;
    }

    public String getPic_cdn_url_235_1() {
        return pic_cdn_url_235_1;
    }

    public void setPic_cdn_url_235_1(String pic_cdn_url_235_1) {
        this.pic_cdn_url_235_1 = pic_cdn_url_235_1;
    }

    public String getPic_cdn_url_16_9() {
        return pic_cdn_url_16_9;
    }

    public void setPic_cdn_url_16_9(String pic_cdn_url_16_9) {
        this.pic_cdn_url_16_9 = pic_cdn_url_16_9;
    }

    public String getPic_cdn_url_3_4() {
        return pic_cdn_url_3_4;
    }

    public void setPic_cdn_url_3_4(String pic_cdn_url_3_4) {
        this.pic_cdn_url_3_4 = pic_cdn_url_3_4;
    }

    public String getPic_cdn_url_1_1() {
        return pic_cdn_url_1_1;
    }

    public void setPic_cdn_url_1_1(String pic_cdn_url_1_1) {
        this.pic_cdn_url_1_1 = pic_cdn_url_1_1;
    }

    public String getCover_img() {
        return cover_img;
    }

    public void setCover_img(String cover_img) {
        this.cover_img = cover_img;
    }

    public CoverImgThemeColor getCover_img_theme_color() {
        return cover_img_theme_color;
    }

    public void setCover_img_theme_color(CoverImgThemeColor cover_img_theme_color) {
        this.cover_img_theme_color = cover_img_theme_color;
    }

    public LineInfo getLine_info() {
        return line_info;
    }

    public void setLine_info(LineInfo line_info) {
        this.line_info = line_info;
    }

    public int getCopyright_stat() {
        return copyright_stat;
    }

    public void setCopyright_stat(int copyright_stat) {
        this.copyright_stat = copyright_stat;
    }

    public int getIs_rumor_refutation() {
        return is_rumor_refutation;
    }

    public void setIs_rumor_refutation(int is_rumor_refutation) {
        this.is_rumor_refutation = is_rumor_refutation;
    }

    public int getMulti_picture_cover() {
        return multi_picture_cover;
    }

    public void setMulti_picture_cover(int multi_picture_cover) {
        this.multi_picture_cover = multi_picture_cover;
    }

    public List<?> getShare_imageinfo() {
        return share_imageinfo;
    }

    public void setShare_imageinfo(List<?> share_imageinfo) {
        this.share_imageinfo = share_imageinfo;
    }

    public String getFakeid() {
        return fakeid;
    }

    public void setFakeid(String fakeid) {
        this.fakeid = fakeid;
    }
}
