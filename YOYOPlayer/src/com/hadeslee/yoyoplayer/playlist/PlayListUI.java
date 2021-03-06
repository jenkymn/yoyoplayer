/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hadeslee.yoyoplayer.playlist;

import com.hadeslee.yoyoplayer.util.MultiImageBorder;
import com.hadeslee.yoyoplayer.player.ui.PlayerUI;
import com.hadeslee.yoyoplayer.tag.SongInfoDialog;
import com.hadeslee.yoyoplayer.util.Config;
import com.hadeslee.yoyoplayer.util.FileNameFilter;
import com.hadeslee.yoyoplayer.util.Util;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 *
 * @author hadeslee
 */
@SuppressWarnings("unchecked")
public class PlayListUI extends JPanel implements ActionListener, MouseListener,
        MouseMotionListener {

    private static final long serialVersionUID = 20071214L;
    private static Logger log = Logger.getLogger(PlayListUI.class.getName());
    private PlayListItem currentItem;//??????????????????
    private PlayList currentPlayList;//??????????????????????????????
    private PlayerUI player;//???????????????UI??????????????????
    private JList leftList, rightList;//????????????????????????
    private JSplitPane split;//???????????????
    private Config config;//??????????????????
    private final DataFlavor flavor = new DataFlavor(MyData.class, "????????????");
    private Vector<PlayList> playlists;//?????????????????????
    private static final Color BG = new Color(6, 6, 6);
    private static final Color FORE = new Color(100, 100, 100);
    private static final Color HILIGHT = new Color(0, 244, 245);
    private int rightIndex = -1;//????????????????????????????????????????????????
    private int leftIndex = -1;//?????????????????????
    private int onIndex = -1;//????????????????????????????????????,???????????????????????????,???????????????tooltip???
    private boolean rightHasFocus;//???????????????????????????????????????
    private List<PlayListItem> clip;//?????????????????????????????????

    public PlayListUI() {
        super(new BorderLayout());
        clip = new ArrayList<PlayListItem>();
        this.setMinimumSize(new Dimension(285, 100));
        this.setPreferredSize(new Dimension(285, 155));
        this.setBackground(Config.getConfig().getPlaylistBackground1());
//        this.setBackground(Color.RED);
    }

    public void setPlayerUI(PlayerUI player) {
        this.player = player;
    }

    /**
     * ??????????????????????????????,
     * @param item
     */
    public void setCurrentItem(PlayListItem item) {
        this.currentItem = item;
        rightList.setSelectedValue(item, true);
        rightList.clearSelection();
    }

    public void loadUI(Component parent, Config config) {
        this.config = config;
        playlists = config.getPlayLists();
        if (playlists.size() == 0) {
            currentPlayList = new BasicPlayList(config);
            currentPlayList.setName(Config.getResource("playlistL.PreNEW") + 1);
            playlists.add(currentPlayList);
        } else {
            //??????????????????????????????????????????????????????,????????????,????????????
            String current = config.getCurrentPlayListName();
            if (current != null) {
                boolean find = false;
                for (PlayList pl : playlists) {
                    if (pl.getName().equals(current)) {
                        currentPlayList = pl;
                        find = true;
                        break;
                    }
                }
                if (find == false) {
                    currentPlayList = playlists.get(0);
                }
            } else {
                currentPlayList = playlists.get(0);
            }
        }
        player.setPlayList(currentPlayList);
        MultiImageBorder border = new MultiImageBorder(parent, config);
        border.setCorner1(Util.getImage("playlist/corner1.png"));
        border.setCorner2(Util.getImage("playlist/corner2.png"));
        border.setCorner3(Util.getImage("playlist/corner3.png"));
        border.setCorner4(Util.getImage("playlist/corner4.png"));
        border.setTop(Util.getImage("playlist/top.png"));
        border.setBottom(Util.getImage("playlist/bottom.png"));
        border.setLeft(Util.getImage("playlist/left.png"));
        border.setRight(Util.getImage("playlist/right.png"));
        this.setBorder(border);
        this.addMouseListener(border);
        this.addMouseMotionListener(border);
        initUI();
    }

    private void initUI() {
        leftList = new JList();
        rightList = new JList();
        leftList.setBackground(BG);
        rightList.setBackground(BG);
        leftList.setListData(playlists);
        leftList.setCellRenderer(new LeftListCellRenderer());
        rightList.setListData(currentPlayList.getAllItems());
        rightList.setCellRenderer(new RightListCellRenderer());
        leftList.addMouseListener(this);
        rightList.addMouseListener(this);
        rightList.addMouseMotionListener(this);

        leftList.setBorder(new EmptyBorder(0, 0, 0, 0));
        rightList.setBorder(new EmptyBorder(0, 0, 0, 0));

        JScrollPane jsp1 = new JScrollPane(leftList);
        JScrollPane jsp2 = new JScrollPane(rightList);
//        jsp1.setBorder(new EmptyBorder(0, 0, 0, 0));
//        jsp2.setBorder(new EmptyBorder(0, 0, 0, 0));
        BasicScrollBarUI yoyo1 = new YOYOScrollBarUI();
        BasicScrollBarUI yoyo2 = new YOYOScrollBarUI();
        jsp1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jsp2.getVerticalScrollBar().setUI(yoyo1);
        jsp1.getVerticalScrollBar().setUI(yoyo2);
        jsp2.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!config.getReadTagInfoStrategy().equals(Config.READ_WHEN_DISPLAY)) {
                    return;
                }
                //???????????????????????????????????????????????????????????????
                if (e.getValueIsAdjusting()) {
                    return;
                }
                int from = rightList.getFirstVisibleIndex();
                int to = rightList.getLastVisibleIndex();
                if (from == -1 || to == -1) {
                    return;
                }
                for (int i = from; i <= to; i++) {
                    currentPlayList.getItemAt(i).getTagInfo();
                }
            }
        });


        jsp1.setBorder(new EmptyBorder(0, 0, 0, 0));
        jsp2.setBorder(new EmptyBorder(0, 0, 0, 0));

        Color backgdColor = new Color(236, 233, 216);
        UIDefaults uidefs = UIManager.getLookAndFeelDefaults();
        uidefs.put("SplitPane.background", new ColorUIResource(BG));
//        uidefs.put("SplitPane.background", new ColorUIResource(backgdColor));
        uidefs.put("SplitPane.foreground", new ColorUIResource(FORE));

        AzSplitPaneUI azSplitPaneUI = new AzSplitPaneUI();
        ImageIcon splitLeftIcon = new ImageIcon(Util.getImage("playlist/splitLeftIcon.gif"));

        ImageIcon splitLeftFocusIcon = new ImageIcon(
                Util.getImage("playlist/splitLeftIcon1.gif"));

        ImageIcon splitRightIcon = new ImageIcon(Util.getImage("playlist/splitRightIcon.gif"));

        ImageIcon splitRightFocusIcon = new ImageIcon(
                Util.getImage("playlist/splitRightIcon1.gif"));
        azSplitPaneUI.setImageIcons(splitLeftIcon, splitLeftFocusIcon,
                splitRightIcon, splitRightFocusIcon);

        split = new JSplitPane(SwingConstants.VERTICAL, true, jsp1, jsp2);
        split.setBorder(new EmptyBorder(0, 0, 0, 0));
        split.setDividerLocation(60);
        split.setUI(azSplitPaneUI);
        split.setOneTouchExpandable(true);

        this.add(split);
        initDragList();

        //??????????????????,???????????????????????????????????????
        rightList.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent ke) {
                if (rightIndex != -1 && rightIndex < currentPlayList.getPlaylistSize()) {
                    if (ke.getKeyCode() == KeyEvent.VK_DELETE) {
                        currentPlayList.removeItemAt(rightIndex);
                        rightList.setListData(currentPlayList.getAllItems());
                        if (rightIndex > currentPlayList.getPlaylistSize() - 1) {
                            rightIndex = 0;
                        }
                        if (currentPlayList.getPlaylistSize() == 0) {
                            return;
                        }
                        rightList.setSelectedValue(currentPlayList.getItemAt(rightIndex), rightHasFocus);
                    }
                }
            }
        });
    }

    /**
     * ?????????????????????????????????
     */
    private void initDragList() {

        DragSource ds = DragSource.getDefaultDragSource();
        ds.createDefaultDragGestureRecognizer(rightList, DnDConstants.ACTION_COPY_OR_MOVE, new DragGestureListener() {

            public void dragGestureRecognized(DragGestureEvent dge) {
                dge.startDrag(DragSource.DefaultCopyDrop, new Transferable() {

                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[]{flavor};
                    }

                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return flavor.equals(flavor);
                    }

                    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                        if (isDataFlavorSupported(flavor)) {
                            if (rightIndex == -1) {
                                return null;
                            }
                            MyData<PlayListItem> my = new MyData<PlayListItem>(rightIndex, currentPlayList.getItemAt(rightIndex));
                            return my;
                        } else {
                            throw new UnsupportedFlavorException(flavor);
                        }
                    }
                });
            }
        });
        rightList.setTransferHandler(new TransferHandler() {

            private static final long serialVersionUID = 20071214L;

            @Override
            public boolean canImport(TransferSupport support) {
                if (!config.isCanDnD()) {
                    return false;
                }
                String os = System.getProperty("os.name");
                if (os.startsWith("Windows")) {
                    return (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                            || support.isDataFlavorSupported(flavor));
                } else if (os.startsWith("Linux")) {
                    return support.isDataFlavorSupported(DataFlavor.stringFlavor)
                            || support.isDataFlavorSupported(flavor);
                } else {
                    return super.canImport(support);
                }

            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    if (!canImport(support)) {
                        return false;
                    }
                    Object toSelect = null;
                    int index = 0;
                    try {
                        JList.DropLocation location = (JList.DropLocation) support.getDropLocation();
                        index = location.getIndex();
                    } catch (Exception exe) {
                        exe.printStackTrace();
                    }
                    if (index < 0) {
                        log.log(Level.SEVERE, "????????????index???????????????0?????????!!");
                        index = 0;
                    }
                    Transferable trans = support.getTransferable();
                    Object obj = null;
                    if (trans.isDataFlavorSupported(flavor)) {
                        obj = trans.getTransferData(flavor);
                        if (obj == null) {
                            return false;
                        }
                        MyData<PlayListItem> my = (MyData<PlayListItem>) obj;
                        currentPlayList.addItemAt(my.getData(), index);
                        if (index < my.getOldIndex()) {
                            currentPlayList.removeItemAt(my.getOldIndex() + 1);
                        } else {
                            currentPlayList.removeItemAt(my.getOldIndex());
                        }
                        toSelect = my.getData();
                    } else if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        obj = trans.getTransferData(DataFlavor.javaFileListFlavor);

                        List<File> s = (List<File>) obj;
                        FileNameFilter ff = new FileNameFilter(Config.EXTS,
                                Config.getResource("playlist.filechooser.name"), true);
                        for (File f : s) {
                            if (f.exists()) {
                                toSelect = addFiles(f, ff, index);
                            }
                        }

                    } else if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)
                            && System.getProperty("os.name").startsWith("Linux")) {
                        obj = trans.getTransferData(DataFlavor.stringFlavor);
                        log.info("?????????????????????" + obj);
                        String[] ss = obj.toString().split("\r\n");
                        FileNameFilter ff = new FileNameFilter(Config.EXTS,
                                Config.getResource("playlist.filechooser.name"), true);
                        for (String s : ss) {
                            try {
                                File f = new File(new URI(s));
                                toSelect = addFiles(f, ff, index);
                            } catch (Exception exe) {
                                exe.printStackTrace();
                            }
                        }
                    }
                    rightList.setListData(currentPlayList.getAllItems());
                    rightList.setSelectedValue(toSelect, true);
                    return true;
                } catch (UnsupportedFlavorException ex) {
                    Logger.getLogger(PlayListUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(PlayListUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                return super.importData(support);
            }
        });
        rightList.setDropMode(DropMode.INSERT);
    }

    /**
     * ???????????????????????????????????????,????????????????????????,??????
     * ??????????????????
     * @param f ??????????????????
     * @param ff ?????????
     * @param index ?????????????????????
     * @return ?????????????????????
     */
    private Object addFiles(File f, FileNameFilter ff, int index) {
        Object toSelect = null;
        if (f.exists()) {
            //???????????????????????????????????????
            //?????????????????????,??????????????????
            if (f.isFile() && ff.accept(f)) {
                PlayListItem item = new PlayListItem(Util.getSongName(f), f.getPath(), -1, true);
                currentPlayList.addItemAt(item, index);
                toSelect = item;
                //????????????????????????????????????????????????????????????????????????
            } else if (f.isDirectory()) {
                File[] fs = f.listFiles(ff);
                for (File file : fs) {
                    toSelect = addFiles(file, ff, index);
                }
            }
        }
        return toSelect;
    }

    public void setPlaylist(PlayList playlist) {
        if (playlist == currentPlayList) {
            return;
        }
        rightHasFocus = false;
        this.currentPlayList = playlist;
        player.setPlayList(playlist);
        config.setCurrentPlayListName(playlist.getName());
        rightList.setListData(currentPlayList.getAllItems());
        rightList.setSelectedValue(player.getCurrentItem(), true);
    }

    public void actionPerformed(ActionEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
            if (e.getSource() == rightList) {
                if (rightIndex == -1) {
                    return;
                }
                PlayListItem pl = currentPlayList.getItemAt(rightIndex);
                log.info("pl=" + pl);
                if (pl != null) {
                    currentPlayList.setItemSelected(pl, rightList.getSelectedIndex());
                    player.setPlayerState(PlayerUI.PLAY);
                    player.setCurrentSong(pl);
                }
            } else if (e.getSource() == leftList) {
                if (leftIndex == -1) {
                    return;
                }
                PlayList pl = playlists.get(leftIndex);
                String s = JOptionPane.showInputDialog(config.getPlWindow(), Config.getResource("playlist.rename.content"));
                if (s != null && !s.trim().equals("")) {
                    pl.setName(s);
                }
                leftList.setListData(playlists);
                repaint();
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        //???????????????????????????????????????????????????
        //??????????????????,?????????JAVA?????????????????????,???????????????
        if (e.getSource() == rightList) {
            rightHasFocus = true;
            rightIndex = rightList.locationToIndex(e.getPoint());
            if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
                rightList.addSelectionInterval(rightIndex, rightIndex);
            } else if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK) {
                rightList.setSelectionInterval(rightList.getAnchorSelectionIndex(), rightIndex);
            } else {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    rightList.setSelectedIndex(rightIndex);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    if (!rightList.isSelectedIndex(rightIndex)) {
                        rightList.setSelectedIndex(rightIndex);
                    }
                }
            }
            rightList.repaint();
        } else if (e.getSource() == leftList) {
//            rightHasFocus = false;
            leftIndex = leftList.locationToIndex(e.getPoint());
            leftList.setSelectedIndex(leftIndex);
            if (leftIndex == -1) {
                return;
            }
            Object obj = playlists.get(leftIndex);
            if (obj != null) {
                this.setPlaylist((PlayList) obj);
            }
            leftList.repaint();
        }
    }

    public void mouseReleased(MouseEvent e) {
//        if (e.getSource() == rightList) {
//            rightHasFocus = true;
//            rightIndex = rightList.locationToIndex(e.getPoint());
////            rightList.setSelectedIndex(rightIndex);
//        }
//        if (e.getButton() == MouseEvent.BUTTON1) {
//            if (e.getSource() == leftList) {
////                Object obj = leftList.getSelectedValue();
////                if (obj != null) {
////                    this.setPlaylist((PlayList) obj);
////                }
//            }
//        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            if (e.getSource() == rightList) {
                showRightMenu(e);
            } else if (e.getSource() == leftList) {
                showLeftMenu(e);
            }
        }
    }

    /**
     * ???????????????????????????
     * @param e ????????????
     */
    private void showLeftMenu(MouseEvent e) {
        JPopupMenu pop = new JPopupMenu();
        //????????????
        pop.add(Config.getResource("playlistL.newplaylist")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int i = playlists.size();
                String name = Config.getResource("playlistL.PreNEW") + (i + 1);
                BasicPlayList plist = new BasicPlayList(config);
                plist.setName(name);
                playlists.add(plist);
                setPlaylist(plist);
                leftList.setListData(playlists);
                leftList.setSelectedValue(plist, true);
                repaint();
//                SwingUtilities.updateComponentTreeUI(leftList);
            }
        });
        //????????????
        pop.add(Config.getResource("playlistL.addplaylist")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                JFileChooser jf = Util.getFileChooser(new FileNameFilter("m3u,pls",
                        Config.getResource("playlist.filechooser.name"), true),
                        JFileChooser.FILES_ONLY);
                int i = jf.showOpenDialog(config.getPlWindow());
                if (i == JFileChooser.APPROVE_OPTION) {
                    BasicPlayList bp = new BasicPlayList(config);
                    boolean b = bp.load(jf.getSelectedFile().getPath());
                    if (b) {
                        playlists.add(bp);
                        setPlaylist(bp);
                        leftList.setListData(playlists);
                        leftList.setSelectedValue(bp, true);
                        repaint();
                        //SwingUtilities.updateComponentTreeUI(leftList);
                        //SwingUtilities.updateComponentTreeUI(rightList);
                    }
                }
            }
        });
        //????????????
        JMenuItem save = new JMenuItem(Config.getResource("playlistL.saveplaylist"));
        save.setEnabled(leftIndex != -1);
        pop.add(save).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                PlayList pl = playlists.get(leftIndex);
                JFileChooser jf = Util.getFileChooser(new FileNameFilter("m3u",
                        Config.getResource("playlist.filechooser.name"), true),
                        JFileChooser.FILES_ONLY);
                jf.setSelectedFile(new File(pl.getName() + ".m3u"));
                int i = jf.showSaveDialog(config.getPlWindow());
                if (i == JFileChooser.APPROVE_OPTION) {
                    PlayList bp = playlists.get(leftIndex);
                    boolean b = bp.save(jf.getSelectedFile().getPath());
                }
            }
        });
        //????????????
        JMenuItem delete = new JMenuItem(Config.getResource("playlistL.deleteplaylist"));
        delete.setEnabled(leftIndex != -1);
        pop.add(delete).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                playlists.remove(leftIndex);
                if (playlists.size() > 0) {
                    if (leftIndex > playlists.size() - 1) {
                        setPlaylist(playlists.get(0));
                    } else {
                        setPlaylist(playlists.get(leftIndex));
                    }
                }
                leftList.setListData(playlists);
                repaint();
                //SwingUtilities.updateComponentTreeUI(leftList);
            }
        });
        pop.addSeparator();
        //????????????
        pop.add(Config.getResource("playlistL.saveall")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                JFileChooser jf = Util.getFileChooser(new FileNameFilter("m3u",
                        Config.getResource("playlist.filechooser.name"), true),
                        JFileChooser.DIRECTORIES_ONLY);
                int i = jf.showSaveDialog(config.getPlWindow());
                if (i == JFileChooser.APPROVE_OPTION) {
                    File f = jf.getSelectedFile();
                    if (!f.exists()) {
                        f.mkdirs();
                    }
                    String dir = f.getPath();
                    for (PlayList pl : playlists) {
                        pl.save(dir + File.separator + pl.getName() + ".m3u");
                    }
                }

            }
        });
        pop.addSeparator();
        //???????????????
        JMenuItem rename = new JMenuItem(Config.getResource("playlistL.rename"));
        rename.setEnabled(leftIndex != -1);
        pop.add(rename).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                PlayList pl = playlists.get(leftIndex);
                String s = JOptionPane.showInputDialog(config.getPlWindow(), Config.getResource("playlist.rename.content"));
                if (s != null && !s.trim().equals("")) {
                    pl.setName(s);
                }
                leftList.setListData(playlists);
                repaint();
                //SwingUtilities.updateComponentTreeUI(leftList);
            }
        });
        pop.addSeparator();
        //???????????????
        pop.add(Config.getResource("playlistL.resort")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Collections.sort(playlists, new Comparator<PlayList>() {

                    public int compare(PlayList o1, PlayList o2) {
                        String s1 = o1.getName();
                        String s2 = o2.getName();
                        return Collator.getInstance(Locale.CHINESE).compare(s1, s2);
                    }
                });
                repaint();
                leftList.setListData(playlists);
                //SwingUtilities.updateComponentTreeUI(leftList);
            }
        });
        pop.show(e.getComponent(), e.getX(), e.getY());
    }

    /**
     * ???????????????????????????
     * @param e ????????????
     */
    private void showRightMenu(MouseEvent e) {
        //??????????????????????????????????????????
        if (false) {
            return;
        }//????????????????????????????????????
        else {
            JPopupMenu pop = new JPopupMenu();
            //????????????
            if (rightIndex != -1) {
                pop.add("<html><b>" + Config.getResource("playlist.play")).addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        PlayListItem pl = currentPlayList.getItemAt(rightIndex);
                        currentPlayList.setItemSelected(pl, rightList.getSelectedIndex());
                        player.setPlayerState(PlayerUI.PLAY);
                        player.setCurrentSong(pl);
//                    player.play();
                    }
                });
            }
//            pop.addSeparator();
            //????????????
            if (rightIndex != -1) {
                pop.add(Config.getResource("playlist.file.property")).addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        PlayListItem pl = currentPlayList.getItemAt(rightIndex);
                        if (pl != null) {
                            new SongInfoDialog(config.getPlWindow(), true, pl).setVisible(true);
                        }
                    }
                });
            }
            pop.addSeparator();
            //????????????
            JMenu add = createAddMenu();
            pop.add(add);
//            pop.addSeparator();
            //????????????
            JMenu delete = createDeleteMenu();
            pop.add(delete);
//            pop.addSeparator();
            //???????????????
            JMenu rename = createRenameMenu();
            pop.add(rename);
//            pop.addSeparator();
            //????????????
            JMenu search = createSearchMenu();
            pop.add(search);
//            pop.addSeparator();
            //????????????
            JMenu sort = createSortMenu();
            pop.add(sort);
//            pop.addSeparator();
            //????????????
            JMenu edit = createEditMenu();
            pop.add(edit);
//            pop.addSeparator();
            //????????????
            JMenu mode = createModeMenu();
            pop.add(mode);
            pop.show(e.getComponent(), e.getX(), e.getY());
            rightList.requestFocus();
        }
    }

    /**
     * ????????????????????????,?????????????????????????????????
     * @return ??????
     */
    private JMenu createRenameMenu() {
        JMenu menu = new JMenu(Config.getResource("playlist.rename"));
        if (rightIndex == -1) {
            menu.setEnabled(false);
            return menu;
        }
        //???????????????????????????????????????????????????,??????????????????????????????
        menu.setEnabled(currentPlayList.getItemAt(rightIndex).isFile());
        //??????.??????????????????
        menu.add(Config.getResource("playlist.rename.songName.ext")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                PlayListItem item = currentPlayList.getItemAt(rightIndex);
                //?????? ????????????????????????????????????,??????????????????
                if (item == player.getCurrentItem()) {
                    player.stop();
                }
                File file = new File(item.getLocation());
                File rename = new File(file.getParent(), item.getTitle() + "." + Util.getType(file));
                boolean b = file.renameTo(rename);
                if (b) {
                    item.setLocation(rename.getPath());
                }
                log.log(Level.INFO, "?????????:" + file + "????????????:" + rename);
                log.log(Level.INFO, "???????????????????" + b);
            }
        });
        //?????? - ?????????.?????????
        menu.add(Config.getResource("playlist.rename.artist.songName.ext")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                PlayListItem item = currentPlayList.getItemAt(rightIndex);
                File file = new File(item.getLocation());
                File rename = new File(file.getParent(),
                        item.getArtist() + " - " + item.getTitle() + "." + Util.getType(file));
                boolean b = file.renameTo(rename);
                if (b) {
                    item.setLocation(rename.getPath());
                }
                log.log(Level.INFO, "?????????:" + file + "????????????:" + rename);
                log.log(Level.INFO, "???????????????????" + b);
            }
        });
        //????????? - ??????.?????????
        menu.add(Config.getResource("playlist.rename.songName.aritst.ext")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                PlayListItem item = currentPlayList.getItemAt(rightIndex);
                File file = new File(item.getLocation());
                File rename = new File(file.getParent(),
                        item.getTitle() + " - " + item.getArtist() + "." + Util.getType(file));
                boolean b = file.renameTo(rename);
                if (b) {
                    item.setLocation(rename.getPath());
                }
                log.log(Level.INFO, "?????????:" + file + "????????????:" + rename);
                log.log(Level.INFO, "???????????????????" + b);
            }
        });
        return menu;
    }

    /**
     * ??????????????????,????????????????????????????????????
     * @return ??????
     */
    private JMenu createAddMenu() {
        JMenu menu = new JMenu(Config.getResource("playlist.add"));
        //????????????
        menu.add(Config.getResource("playlist.add.file")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                JFileChooser jf = Util.getFileChooser(new FileNameFilter(Config.EXTS,
                        Config.getResource("playlist.filechooser.name"), true), JFileChooser.FILES_ONLY);
                int i = jf.showOpenDialog(config.getPlWindow());
                if (i == JFileChooser.APPROVE_OPTION) {
                    File f = jf.getSelectedFile();
                    PlayListItem item = new PlayListItem(Util.getSongName(f), f.getPath(), -1, true);
                    if (rightIndex == -1) {
                        currentPlayList.appendItem(item);
                    } else {
                        currentPlayList.addItemAt(item, rightIndex);
                    }
                    rightList.setListData(currentPlayList.getAllItems());
                }
            }
        });
        //???????????????
        menu.add(Config.getResource("playlist.add.dir")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                JFileChooser jf = Util.getFileChooser(new FileNameFilter(Config.EXTS,
                        Config.getResource("playlist.filechooser.name"), true), JFileChooser.DIRECTORIES_ONLY);
                int i = jf.showOpenDialog(config.getPlWindow());
                if (i == JFileChooser.APPROVE_OPTION) {
                    File f = jf.getSelectedFile();
                    if (f.isDirectory()) {
                        File[] fs = f.listFiles(new FileNameFilter(Config.EXTS,
                                Config.getResource("playlist.filechooser.name"), false));
                        for (File file : fs) {
                            PlayListItem item = new PlayListItem(Util.getSongName(file), file.getPath(), -1, true);
                            if (rightIndex == -1) {
                                currentPlayList.appendItem(item);
                            } else {
                                currentPlayList.addItemAt(item, rightIndex);
                            }
                        }
                        rightList.setListData(currentPlayList.getAllItems());
                    }
                }
            }
        });
        //??????????????????
        menu.add(Config.getResource("playlist.add.url")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String s = JOptionPane.showInputDialog(config.getPlWindow(),
                        Config.getResource("playlist.add.inputurl"));
                if (s != null) {
                    if (Config.startWithProtocol(s)) {
                        PlayListItem item = new PlayListItem(s, s, -1, false);
                        if (rightIndex == -1) {
                            currentPlayList.appendItem(item);
                        } else {
                            currentPlayList.addItemAt(item, rightIndex);
                        }
                        rightList.setListData(currentPlayList.getAllItems());
                    } else {
                        JOptionPane.showMessageDialog(config.getPlWindow(),
                                Config.getResource("playlist.add.invalidUrl"));
                    }
                }
            }
        });
        return menu;
    }

    /**
     * ??????????????????????????????????????????
     * @return ??????
     */
    private JMenu createDeleteMenu() {
        JMenu menu = new JMenu(Config.getResource("playlist.delete"));
        if (rightIndex == -1) {
            menu.setEnabled(false);
            return menu;
        }
        //???????????????
        menu.add(Config.getResource("playlist.delete.select")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Object[] objs = rightList.getSelectedValues();
                for (Object obj : objs) {
                    currentPlayList.removeItem((PlayListItem) obj);
                }
                rightList.setListData(currentPlayList.getAllItems());
                rightList.setSelectedIndex(rightIndex);
            }
        });
        //???????????????
        menu.add(Config.getResource("playlist.delete.repeat")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Vector<PlayListItem> vs = currentPlayList.getAllItems();
                for (int i = 0; i < vs.size() - 1; i++) {
                    PlayListItem item1 = vs.get(i);
                    for (int j = i + 1; j < vs.size(); j++) {
                        PlayListItem item2 = vs.get(j);
                        if (item1.getLocation().equals(item2.getLocation())) {
                            vs.remove(item2);
                            j--;
                        }
                    }
                }
                rightList.setListData(vs);
            }
        });
        //??????????????????
        menu.add(Config.getResource("playlist.delete.error")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Vector<PlayListItem> vs = currentPlayList.getAllItems();
                List<PlayListItem> temp = new ArrayList<PlayListItem>();
                for (PlayListItem item : vs) {
                    //???????????????,???????????????????????????????????????????????????????????????
                    if (item.isFile) {
                        File f = new File(item.getLocation());
                        if (f.exists()) {
                            if (item.getFormattedLength().equals("-1")) {
                                temp.add(item);
                            }
                        } else {
                            temp.add(item);
                        }
                    } else {//???????????????????????????????????????,???????????????
                    }
                }
                vs.removeAll(temp);
                rightList.setListData(vs);
            }
        });

        //????????????
        menu.add(Config.getResource("playlist.delete.all")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                currentPlayList.removeAllItems();
                rightList.setListData(currentPlayList.getAllItems());
            }
        });
        //????????????
        JMenuItem delete = new JMenuItem(Config.getResource("playlist.delete.deletefile"));
        menu.add(delete);
        delete.setEnabled(!config.isDisableDelete());
        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(config.getPlWindow(),
                        Config.getResource("playlist.deletefile.confirm"),
                        Config.getResource("confirm"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    Object[] objs = rightList.getSelectedValues();
                    for (Object obj : objs) {
                        PlayListItem pl = (PlayListItem) obj;
                        if (pl.isFile()) {
                            File f = new File(pl.getLocation());
                            boolean b = f.delete();
                            if (b) {
                                currentPlayList.removeItem(pl);
                                repaint();
                            }
                        }
                    }
                }
            }
        });
        return menu;
    }

    private JMenu createSearchMenu() {
        //???????????????,?????????????????????????????????,
        //??????????????????????????????,??????????????????,??????????????????
        JMenu menu = new JMenu(Config.getResource("playlist.search"));
        //????????????
        menu.add(Config.getResource("playlist.search.fileName")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String s = JOptionPane.showInputDialog(config.getPlWindow(),
                        Config.getResource("playlist.search.inputFileName"));
                if (s != null) {
                    s = s.trim();
                    for (PlayListItem item : currentPlayList.getAllItems()) {
                        String name = Util.getSongName(item.getLocation());
                        if (name.contains(s)) {
                            rightList.setSelectedValue(item, true);
                        }
                    }
                }
            }
        });
        menu.add(Config.getResource("playlist.search.title")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String s = JOptionPane.showInputDialog(config.getPlWindow(),
                        Config.getResource("playlist.search.inputTitle"));
                if (s != null) {
                    s = s.trim();
                    for (PlayListItem item : currentPlayList.getAllItems()) {
                        String name = item.getTitle();
                        if (name.contains(s)) {
                            rightList.setSelectedValue(item, true);
                        }
                    }
                }
            }
        });
        menu.add(Config.getResource("playlist.search.artist")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String s = JOptionPane.showInputDialog(config.getPlWindow(),
                        Config.getResource("playlist.search.inputArtist"));
                if (s != null) {
                    s = s.trim();
                    for (PlayListItem item : currentPlayList.getAllItems()) {
                        String name = item.getArtist();
                        if (name.contains(s)) {
                            rightList.setSelectedValue(item, true);
                        }
                    }
                }
            }
        });
        menu.add(Config.getResource("playlist.search.album")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String s = JOptionPane.showInputDialog(config.getPlWindow(),
                        Config.getResource("playlist.search.inputAlbum"));
                if (s != null) {
                    s = s.trim();
                    for (PlayListItem item : currentPlayList.getAllItems()) {
                        String name = item.getAlbum();
                        if (name.contains(s)) {
                            rightList.setSelectedValue(item, true);
                        }
                    }
                }
            }
        });
        return menu;
    }

    private JMenu createSortMenu() {
        JMenu menu = new JMenu(Config.getResource("playlist.sort"));
        //?????????
        menu.add(Config.getResource("playlist.sort.artist")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Collections.sort(currentPlayList.getAllItems(), new Comparator<PlayListItem>() {

                    public int compare(PlayListItem o1, PlayListItem o2) {
                        String s1 = o1.getArtist() == null ? "" : o1.getArtist();
                        String s2 = o2.getArtist() == null ? "" : o2.getArtist();
                        return Collator.getInstance(Locale.CHINESE).compare(s1, s2);
                    }
                });
                rightList.setListData(currentPlayList.getAllItems());
            }
        });
        //?????????
        menu.add(Config.getResource("playlist.sort.title")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Collections.sort(currentPlayList.getAllItems(), new Comparator<PlayListItem>() {

                    public int compare(PlayListItem o1, PlayListItem o2) {
                        String s1 = o1.getTitle() == null ? "" : o1.getTitle();
                        String s2 = o2.getTitle() == null ? "" : o2.getTitle();
                        return Collator.getInstance(Locale.CHINESE).compare(s1, s2);
                    }
                });
                rightList.setListData(currentPlayList.getAllItems());
            }
        });
        //?????????
        menu.add(Config.getResource("playlist.sort.album")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Collections.sort(currentPlayList.getAllItems(), new Comparator<PlayListItem>() {

                    public int compare(PlayListItem o1, PlayListItem o2) {
                        String s1 = o1.getAlbum() == null ? "" : o1.getAlbum();
                        String s2 = o2.getAlbum() == null ? "" : o2.getAlbum();
                        return Collator.getInstance(Locale.CHINESE).compare(s1, s2);
                    }
                });
                rightList.setListData(currentPlayList.getAllItems());
            }
        });
        //????????????
        menu.add(Config.getResource("playlist.sort.fileName")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Collections.sort(currentPlayList.getAllItems(), new Comparator<PlayListItem>() {

                    public int compare(PlayListItem o1, PlayListItem o2) {
                        String s1 = o1.getName() == null ? "" : o1.getName();
                        String s2 = o2.getName() == null ? "" : o2.getName();
                        return Collator.getInstance(Locale.CHINESE).compare(s1, s2);
                    }
                });
                rightList.setListData(currentPlayList.getAllItems());
            }
        });
        //???????????????
        menu.add(Config.getResource("playlist.sort.length")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Collections.sort(currentPlayList.getAllItems(), new Comparator<PlayListItem>() {

                    public int compare(PlayListItem o1, PlayListItem o2) {
                        return (int) (o1.getLength() - o2.getLength());
                    }
                });
                rightList.setListData(currentPlayList.getAllItems());
            }
        });
        menu.addSeparator();
        //???????????? 
        menu.add(Config.getResource("playlist.sort.random")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Collections.shuffle(currentPlayList.getAllItems());
                rightList.setListData(currentPlayList.getAllItems());
            }
        });
        return menu;
    }

    private JMenu createEditMenu() {
        JMenu menu = new JMenu(Config.getResource("playlist.edit"));
        //??????
        if (rightIndex != -1) {
            menu.add(Config.getResource("playlist.edit.cut")).addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    clip.clear();
                    Object[] objs = rightList.getSelectedValues();
                    for (Object obj : objs) {
                        PlayListItem item = (PlayListItem) obj;
                        currentPlayList.removeItem(item);
                        clip.add(item);
                    }
                    rightList.setListData(currentPlayList.getAllItems());
                    rightList.setSelectedIndex(rightIndex);
                }
            });
        }
        //??????
        if (rightIndex != -1) {
            menu.add(Config.getResource("playlist.edit.copy")).addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    clip.clear();
                    Object[] objs = rightList.getSelectedValues();
                    for (Object obj : objs) {
                        PlayListItem item = (PlayListItem) obj;
                        clip.add(item);
                    }
                    rightList.setSelectedIndex(rightIndex);
                }
            });
        }
        //??????
        menu.add(Config.getResource("playlist.edit.paste")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (clip.size() > 0) {
                    PlayListItem last = null;
                    for (PlayListItem item : clip) {
                        PlayListItem it = new PlayListItem(item.getName(), item.getLocation(),
                                item.getLength(), item.isFile());
                        int index = rightIndex;
                        if (index == -1) {
                            currentPlayList.appendItem(it);
                        } else {
                            currentPlayList.addItemAt(it, index);
                        }
                        last = it;
                    }
                    rightList.setListData(currentPlayList.getAllItems());
                    rightList.setSelectedValue(last, true);
                    rightList.requestFocus();
                    rightHasFocus = true;
                }
            }
        });
        menu.addSeparator();
        //??????
        menu.add(Config.getResource("playlist.edit.selectAll")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                rightList.setSelectionInterval(0, currentPlayList.getPlaylistSize() - 1);
            }
        });
        //?????????
        menu.add(Config.getResource("playlist.edit.selectNone")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                rightList.clearSelection();
                rightIndex = -1;
            }
        });
        //??????
        menu.add(Config.getResource("playlist.edit.selectReverse")).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int[] indexes = rightList.getSelectedIndices();
                List<Integer> list = new ArrayList<Integer>();
                for (int i = 0; i < currentPlayList.getPlaylistSize(); i++) {
                    boolean has = false;
                    for (int index : indexes) {
                        if (i == index) {
                            has = true;
                        }
                    }
                    if (has == false) {
                        list.add(i);
                    }
                }
                int[] selects = new int[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    selects[i] = list.get(i);
                }
                rightList.setSelectedIndices(selects);
            }
        });
        return menu;
    }

    private JMenu createModeMenu() {
        JMenu menu = new JMenu(Config.getResource("playlist.mode"));
        ButtonGroup bg1 = new ButtonGroup();
        ButtonGroup bg2 = new ButtonGroup();
        //?????????
        JRadioButtonMenuItem noCircle = new JRadioButtonMenuItem(Config.getResource("playlist.mode.noCircle"));
        noCircle.setSelected(!config.isRepeatEnabled());
        menu.add(noCircle).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                config.setRepeatEnabled(false);
            }
        });
        //????????????
        JRadioButtonMenuItem singleCircle = new JRadioButtonMenuItem(Config.getResource("playlist.mode.singleCircle"));
        singleCircle.setSelected(config.isRepeatEnabled() && config.getRepeatStrategy() == Config.REPEAT_ONE);
        menu.add(singleCircle).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                config.setRepeatEnabled(true);
                config.setRepeatStrategy(Config.REPEAT_ONE);
            }
        });
        //????????????
        JRadioButtonMenuItem allCircle = new JRadioButtonMenuItem(Config.getResource("playlist.mode.allCircle"));
        allCircle.setSelected(config.isRepeatEnabled() && config.getRepeatStrategy() == Config.REPEAT_ALL);
        menu.add(allCircle).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                config.setRepeatEnabled(true);
                config.setRepeatStrategy(Config.REPEAT_ALL);
            }
        });
        menu.addSeparator();
        //????????????
        JRadioButtonMenuItem orderPlay = new JRadioButtonMenuItem(Config.getResource("playlist.mode.orderPlay"));
        orderPlay.setSelected(config.getPlayStrategy() == Config.ORDER_PLAY);
        menu.add(orderPlay).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                config.setPlayStrategy(Config.ORDER_PLAY);
            }
        });
        //????????????
        JRadioButtonMenuItem randomPlay = new JRadioButtonMenuItem(Config.getResource("playlist.mode.randomPlay"));
        randomPlay.setSelected(config.getPlayStrategy() == Config.RANDOM_PLAY);
        menu.add(randomPlay).addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {


                config.setPlayStrategy(
                        Config.RANDOM_PLAY);
            }
        });
        bg1.add(noCircle);
        bg1.add(singleCircle);
        bg1.add(allCircle);
        bg2.add(orderPlay);
        bg2.add(randomPlay);
        return menu;
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    /**
     * ??????????????????????????????????????????????????????
     * @param T
     */
    private class MyData<T> {

        private int oldIndex;
        private T t;

        public MyData(int oldIndex, T t) {
            this.oldIndex = oldIndex;
            this.t = t;
        }

        public int getOldIndex() {
            return oldIndex;
        }

        public T getData() {
            return t;
        }
    }

    /**
     * ????????????????????????
     */
    private class LeftListCellRenderer extends JLabel implements ListCellRenderer {

        private static final long serialVersionUID = 20071214L;
        private volatile boolean hasFocus;

        public LeftListCellRenderer() {
            this.setOpaque(true);
            this.setHorizontalAlignment(JLabel.CENTER);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            this.setText(value.toString());
            hasFocus = cellHasFocus;
            if (value.equals(currentPlayList)) {
                isSelected = true;
            } else {
                isSelected = false;
            }
            if (isSelected) {
                setBackground(BG);
                setForeground(HILIGHT);
            } else {
                setBackground(BG);
                setForeground(FORE);
            }
            if (cellHasFocus) {
                setForeground(BG);
                setBackground(FORE);
            }
            return this;
        }

        public void paint(Graphics g) {
            super.paint(g);
            if (hasFocus) {
                g.setColor(HILIGHT);
                g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }
        }

        public void repaint() {
        }

        public void repaint(Rectangle rec) {
        }

        public void repaint(long l, int x, int y, int width, int height) {
        }

        public void validate() {
        }

        public void invalidate() {
        }

        public void revalidate() {
        }

        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        }
    }

    /**
     * ???????????????????????????
     */
    private class RightListCellRenderer extends YOYOLabel implements ListCellRenderer {

        private static final long serialVersionUID = 20071214L;

        public RightListCellRenderer() {
            this.setOpaque(true);
            this.setBorder(new EmptyBorder(0, 0, 0, 0));
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            this.setText("hadeslee");
            PlayListItem item = (PlayListItem) value;
            if (item == player.getCurrentItem()) {
                item.setSelected(true);
            } else {
                item.setSelected(false);
            }
            this.setFont(config.getPlaylistFont());
            list.setFont(config.getPlaylistFont());
            this.setPlayListItem(item);
            this.setIsSelected(isSelected && rightHasFocus);
            this.setIndex(index);
            this.setItemCount(currentPlayList.getPlaylistSize());
//           this.setHasFocus(rightHasFocus && (cellHasFocus || (rightIndex == index)));
            this.setHasFocus(rightHasFocus && (cellHasFocus));
//            if (cellHasFocus || isSelected) {
////                setBackground(Color.WHITE);
////                setForeground(Color.BLACK);
//            } else {
//                setForeground(FORE);
//                setBackground(BG);
//            }
            if (index % 2 == 0) {
                setBackground(config.getPlaylistBackground1());
            } else {
                setBackground(config.getPlaylistBackground2());
            }
            return this;
        }

        public void repaint() {
        }

        public void repaint(Rectangle rec) {
        }

        public void repaint(long l, int x, int y, int width, int height) {
        }

        public void validate() {
        }

        public void invalidate() {
        }

        public void revalidate() {
        }

        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        }
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        int index = rightList.locationToIndex(e.getPoint());
        if (index != onIndex) {
            onIndex = index;
            showInfo();
        }
    }

    /**
     * ??????ToolTip?????????
     */
    private void showInfo() {
        if (onIndex == -1 || !config.isShowTooltipOnPlayList()) {
            rightList.setToolTipText(null);
            return;
        }
        PlayListItem item = currentPlayList.getItemAt(onIndex);
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append(Config.getResource("songinfo.title")).append(" ").append(item.getTitle()).append("<br>");
        sb.append(Config.getResource("songinfo.artist")).append(" ").append(item.getArtist()).append("<br>");
        sb.append(Config.getResource("songinfo.album")).append(" ").append(item.getAlbum()).append("<br>");
        sb.append(Config.getResource("songinfo.format")).append(" ").append(item.getFormat()).append("<br>");
        sb.append(Config.getResource("songinfo.length")).append(" ").append(item.getFormattedLength()).append("<br>");
        sb.append(Config.getResource("songinfo.location")).append(" ").append(item.getLocation()).append("<br>&nbsp<p>");
        sb.append("</html>");
        rightList.setToolTipText(sb.toString());
    }
}
