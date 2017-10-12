
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by cpdu on 2017/7/30.
 */
public class Gui extends JFrame implements ActionListener{

    JTextField inputFilePath = new JTextField();
    JButton inputButton = new JButton("选择文件");
    JTextField outputFilePath = new JTextField();
    JButton outputButton = new JButton("选择目录");
    JButton extractButton = new JButton("提取");
    JProgressBar bar = new JProgressBar();
    JLabel taskLabel = new JLabel();
    JList<CheckboxListItem> partList = null;
    JPanel panel = new JPanel();

    HashMap<String, MkvComp> comp = new HashMap<>();
    String filename = "";
    int taskNum = 0;

    public Gui(String title){
        super(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(200,100,0,0);

        JPanel inputSelect = new JPanel();
        inputSelect.setLayout(new BorderLayout());
        JLabel inputLabel = new JLabel("  输入文件：");
        inputSelect.add(inputLabel, BorderLayout.WEST);
        inputFilePath.setEditable(false);
        inputFilePath.setTransferHandler(new TransferHandler()
        {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean importData(JComponent comp, Transferable t) {
                try {
                    Object o = t.getTransferData(DataFlavor.javaFileListFlavor);
                    String filepath = o.toString();
                    if (filepath.startsWith("[")) {
                        filepath = filepath.substring(1);
                    }
                    if (filepath.endsWith("]")) {
                        filepath = filepath.substring(0, filepath.length() - 1);
                    }
                    if(filepath.endsWith(".mkv") || filepath.endsWith(".MKV")){
                        inputFilePath.setText(filepath);
                        refreshList();
                    }else {
                        showMessage("仅支持处理mkv文件。", JOptionPane.WARNING_MESSAGE);
                    }
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
            @Override
            public boolean canImport(JComponent comp, DataFlavor[] flavors) {
                for (int i = 0; i < flavors.length; i++) {
                    if (DataFlavor.javaFileListFlavor.equals(flavors[i])) {
                        return true;
                    }
                }
                return false;
            }
        });
        inputSelect.add(inputFilePath, BorderLayout.CENTER);
        inputButton.addActionListener(this);
        inputSelect.add(inputButton, BorderLayout.EAST);

        partList = new JList<CheckboxListItem>(new CheckboxListItem[] {  });
        partList.setCellRenderer(new CheckboxListRenderer());
        partList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        partList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if(!partList.isEnabled()) return;
                JList<CheckboxListItem> list = (JList<CheckboxListItem>) event.getSource();
                if(list.getModel().getSize() == 0) return;
                int index = list.locationToIndex(event.getPoint());
                CheckboxListItem item = (CheckboxListItem) list.getModel().getElementAt(index);
                item.setSelected(!item.isSelected());
                list.repaint(list.getCellBounds(index, index));
                String line = partList.getSelectedValue().toString();
                MkvComp component = comp.get(line);
                if(component.getSelected()) taskNum--;
                else taskNum++;
                taskLabel.setText("  已完成0 / 待完成" + taskNum + "  ");
                component.setSelected(!component.getSelected());
                bar.setValue(0);
            }
        });
        JScrollPane scrollPane = new JScrollPane(partList);

        JPanel outputSelect = new JPanel();
        outputSelect.setLayout(new BorderLayout());
        JLabel outputLabel = new JLabel("  输出目录：");
        outputSelect.add(outputLabel, BorderLayout.WEST);
        outputFilePath.setEditable(false);
        outputSelect.add(outputFilePath, BorderLayout.CENTER);
        outputButton.addActionListener(this);
        outputSelect.add(outputButton, BorderLayout.EAST);

        bar.setOrientation(JProgressBar.HORIZONTAL);
        bar.setMinimum(0);
        bar.setMaximum(100);
        bar.setValue(0);
        extractButton.addActionListener(this);
        taskLabel.setText("  已完成0 / 待完成0  ");

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(extractButton, BorderLayout.WEST);
        bottomPanel.add(bar, BorderLayout.CENTER);
        bottomPanel.add(taskLabel, BorderLayout.EAST);

        JPanel extractPanel = new JPanel();
        extractPanel.setLayout(new BorderLayout());
        extractPanel.add(outputSelect, BorderLayout.NORTH);
        extractPanel.add(bottomPanel, BorderLayout.SOUTH);

        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(700, 500));
        panel.add(inputSelect, BorderLayout.NORTH);
        panel.add(extractPanel, BorderLayout.SOUTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        this.getRootPane().setDefaultButton(extractButton);

        Container container = this.getContentPane();
        container.add(panel);

        this.pack();
        this.setVisible(true);
    }

    public void refreshList(){
        String inputPath = inputFilePath.getText();
        String path[] = inputPath.split("/");
        String outputPath = "";
        for(int i = 0; i < path.length - 1; i++){
            outputPath += path[i];
            outputPath += "/";
        }
        outputFilePath.setText(outputPath);
        filename = path[path.length - 1];
        if(filename.endsWith(".mkv")) filename = filename.substring(0, filename.length()-4);
        taskLabel.setText("  已完成0 / 待完成0  ");
        taskNum = 0;
        bar.setValue(0);

        comp.clear();
        String[] cmds = {"mkvmerge", "-i", inputPath};
        BufferedReader reader = null;
        try {
            //execute command
            Process proc = Runtime.getRuntime().exec(cmds);

            //read result
            reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            reader.readLine();
            ArrayList<String> dataArr = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                comp.put(line, new MkvComp(line));
                dataArr.add(line);
            }

            //refresh list data
            CheckboxListItem[] data = new CheckboxListItem[comp.size()];
            for(int i = 0; i < dataArr.size(); i++){
                data[i] = new CheckboxListItem(dataArr.get(i));
            }
            partList.setListData(data);
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if(cmd.equals("选择文件")){

            //open file choosing dialog
            JFileChooser inputFile = new JFileChooser();
            inputFile.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().endsWith(".mkv");
                }
                @Override
                public String getDescription() {
                    return ".mkv";
                }
            });
            inputFile.showOpenDialog(this);

            //set input textfield
            if(inputFile.getSelectedFile() != null){
                inputFilePath.setText(inputFile.getSelectedFile().getAbsolutePath());
            }

            //refresh list
            refreshList();
        }else if(cmd.equals("选择目录")){
            JFileChooser outputFile = new JFileChooser();
            outputFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            outputFile.showOpenDialog(this);
            String path = outputFile.getSelectedFile().getAbsolutePath();
            if(path != null) outputFilePath.setText(path);
        }else if(cmd.equals("提取")){

            //verify whether the file exists
            File f = new File(inputFilePath.getText());
            if(!f.exists()) {
                showMessage("文件不存在。", JOptionPane.WARNING_MESSAGE);
                return;
            }

            //verify whether task exists
            if(taskNum == 0){
                showMessage("未选择提取项目。", JOptionPane.WARNING_MESSAGE);
                return;
            }

            //disable unrelated parts
            inputButton.setEnabled(false);
            outputButton.setEnabled(false);
            partList.setEnabled(false);
            extractButton.setEnabled(false);

            //execute the command
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int completedNum = 0;
                    for(String i : comp.keySet()){
                        MkvComp component = comp.get(i);
                        if(!component.getSelected()) continue;


                        String extractTarg = null;
                        if(component.getCategory().equals("Track"))
                            extractTarg = component.getId()+":"+filename+"_track"+component.getId()+component.getExtName();
                        else if(component.getCategory().equals("Attachment"))
                            extractTarg = component.getId();
                        else if(component.getCategory().equals("Chapter"))
                            extractTarg = filename + "_chapter.xml";

                        String[] cmds = {"mkvextract", component.getCategory().toLowerCase()+"s", inputFilePath.getText(), extractTarg};
                        if(component.getCategory().equals("Chapter")) {
                            cmds = Arrays.copyOf(cmds, 5);
                            cmds[4] = cmds[3];
                            cmds[3] = "--redirect-output";
                        }

                        BufferedReader reader = null;
                        try {
                            Process proc = Runtime.getRuntime().exec(cmds, null, new File(outputFilePath.getText()));
                            if(component.getCategory().equals("Track")){
                                reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                                String line = "";
                                reader.readLine();
                                completedNum++;
                                while ((line = reader.readLine()) != null) {
                                    String percent = line.split(" ")[1];
				    if(!percent.matches("^[1-9]+%$")) continue;
                                    int globalpercent = 100 * (completedNum-1) / taskNum + Integer.parseInt(percent.substring(0, percent.length()-1)) / taskNum;
                                    bar.setValue(globalpercent);
                                }
                            }else{
                                bar.setValue(100 * (++completedNum) / taskNum);
                            }
                            taskLabel.setText("  已完成" + completedNum + " / 待完成" + taskNum + "  ");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } finally {
                            try {
                                if(reader != null) reader.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                    bar.setValue(100);
                    showMessage("提取成功", JOptionPane.INFORMATION_MESSAGE);

                    //enable unrelated parts
                    inputButton.setEnabled(true);
                    outputButton.setEnabled(true);
                    partList.setEnabled(true);
                    extractButton.setEnabled(true);
                }
            }).start();
        }

    }

    public void showMessage(String message, int type){
        JOptionPane.showMessageDialog(this, message, "提示", type);
    }
    
    public static void main(String[] args){
        new Gui("MKVExtractor based on MKVToolNix by CP.Du");
    }

}
