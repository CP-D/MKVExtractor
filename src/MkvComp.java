/**
 * Created by cpdu on 2017/8/1.
 */
public class MkvComp {
    private String category;
    private String id;
    private boolean isSelected;
    private String extName="";

    public MkvComp(String line){
        String[] word = line.split(" ");
        this.category = word[0];
        category = category.endsWith(":") ? category.substring(0, category.length()-2) : category;
        this.id = word[2].substring(0, word[2].length()-1);
        this.isSelected = false;
        if(category.equals("Attachment") || category.equals("Chapter")) return;
        if(word[word.length-1].equals("(SubStationAlpha)")) this.extName = ".ass";
        else if(word[word.length-1].equals("(SubRip/SRT)")) this.extName = ".srt";
        else if(word[word.length-1].contains("AVC")) this.extName = ".avc";
        else if(word[word.length-1].contains("AC-3")) this.extName = ".ac3";
        else if(word[word.length-1].contains("HEVC")) this.extName = ".hevc";
        else if(word[word.length-1].contains("FLAC")) this.extName = ".flac";
        else if(word[word.length-1].contains("DTS")) this.extName = ".dts";
        else if(word[word.length-1].equals("(VobSub)")) this.extName = ".sub";
        else if(word[word.length-1].equals("(TrueHD)")) this.extName = ".thd";
        else if(word[word.length-1].equals("(AAC)")) this.extName = ".aac";
        else if(word[word.length-1].contains("PGS")) this.extName = ".sup";
    }


    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public boolean getSelected() {
        return isSelected;
    }

    public String getExtName() {
        return extName;
    }
}
