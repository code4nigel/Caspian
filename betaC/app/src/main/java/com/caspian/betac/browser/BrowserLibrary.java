package com.caspian.betac.browser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.caspian.betac.BookmarkManager;
import com.caspian.betac.HistoryManager;
import com.caspian.betac.MainActivity;
import com.caspian.betac.MainActivity.TabItem;
import com.caspian.betac.R;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Bookmarks, history and their import/export UI. */
public final class BrowserLibrary {
    public static final int REQUEST_CODE_EXPORT_BOOKMARKS_TREE = 9410;
    public static final int REQUEST_CODE_IMPORT_BOOKMARKS_FILE = 9411;

    private final MainActivity activity;
    private final BooleanSupplier dark;
    private final BooleanSupplier motion;
    private final Supplier<MainActivity.TabItem> activeTab;
    private final Consumer<String> sound;
    private final Function<String, String> displayUrl;
    private final BookmarkManager bookmarkManager;
    private Runnable currentBookmarksRefreshRunnable;

    public BrowserLibrary(MainActivity activity, BooleanSupplier dark, BooleanSupplier motion,
                          Supplier<MainActivity.TabItem> activeTab, Consumer<String> sound,
                          Function<String, String> displayUrl) {
        this.activity = activity;
        this.dark = dark;
        this.motion = motion;
        this.activeTab = activeTab;
        this.sound = sound;
        this.displayUrl = displayUrl;
        bookmarkManager = new BookmarkManager(activity);
    }

    private int dpToPx(int value) { return BrowserChrome.dp(activity, value); }

    public void showBookmarksDialog() {
        showBookmarksDialog(0);
    }

    public void showBookmarksDialog(int enterDirection) {
        sound.accept("tap");
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_bookmarks, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(dark.getAsBoolean() ? 0xFF070A10 : 0xFFF8FAFC));
            dialog.getWindow().setWindowAnimations(0);
        }
        dialogView.setBackgroundColor(dark.getAsBoolean() ? 0xFF070A10 : 0xFFF8FAFC);

        // Single iOS Slide Entrance Transition
        if (motion.getAsBoolean() && enterDirection > 0) {
            dialogView.setTranslationX(dpToPx(80));
            dialogView.setAlpha(0.3f);
            dialogView.animate().translationX(0f).alpha(1f).setDuration(200).setInterpolator(new DecelerateInterpolator(1.6f)).start();
        } else if (motion.getAsBoolean() && enterDirection < 0) {
            dialogView.setTranslationX(-dpToPx(80));
            dialogView.setAlpha(0.3f);
            dialogView.animate().translationX(0f).alpha(1f).setDuration(200).setInterpolator(new DecelerateInterpolator(1.6f)).start();
        }

        // Header Views
        View bookmarksRoot = dialogView.findViewById(R.id.bookmarks_root);
        View headerBar = dialogView.findViewById(R.id.bookmarks_header_bar);
        ImageButton btnBack = dialogView.findViewById(R.id.btn_bookmarks_back);
        View capsuleCenter = dialogView.findViewById(R.id.capsule_bookmarks_center);
        ImageView iconBookmarksHeader = dialogView.findViewById(R.id.icon_bookmarks_header);
        TextView textBookmarksHeader = dialogView.findViewById(R.id.text_bookmarks_header);
        TextView badgeHeaderCount = dialogView.findViewById(R.id.badge_bookmarks_header_count);
        TextView btnGotoHistory = dialogView.findViewById(R.id.btn_bookmarks_goto_history);
        TextView btnGotoSettings = dialogView.findViewById(R.id.btn_bookmarks_goto_settings);

        // Content & Toolbar Views
        TextView textVaultTitle = dialogView.findViewById(R.id.text_vault_title);
        TextView badgeVaultSaved = dialogView.findViewById(R.id.badge_vault_saved);
        TextView btnBatchToggle = dialogView.findViewById(R.id.btn_bookmarks_batch_toggle);
        TextView btnBatchDelete = dialogView.findViewById(R.id.btn_bookmarks_batch_delete);



        View searchBar = dialogView.findViewById(R.id.bookmarks_search_bar);
        ImageView iconSearch = dialogView.findViewById(R.id.icon_bookmarks_search);
        EditText searchInput = dialogView.findViewById(R.id.bookmarks_search_input);
        View btnSort = dialogView.findViewById(R.id.btn_bookmarks_sort);
        TextView textSortLabel = dialogView.findViewById(R.id.text_bookmarks_sort_label);

        // Filter Chips
        TextView chipAll = dialogView.findViewById(R.id.chip_filter_all);
        TextView chipFav = dialogView.findViewById(R.id.chip_filter_favorites);
        LinearLayout layoutChips = dialogView.findViewById(R.id.layout_filter_chips);

        LinearLayout listContainer = dialogView.findViewById(R.id.bookmarks_list_container);
        View emptyView = dialogView.findViewById(R.id.layout_bookmarks_empty);
        TextView textEmpty = dialogView.findViewById(R.id.text_bookmarks_empty);

        // Bottom Action Dock
        View bottomDock = dialogView.findViewById(R.id.bookmarks_bottom_dock);
        View btnDockExport = dialogView.findViewById(R.id.btn_dock_export);
        View btnDockAdd = dialogView.findViewById(R.id.btn_dock_add_folder);
        View btnDockImport = dialogView.findViewById(R.id.btn_dock_import);
        TextView textDockExport = dialogView.findViewById(R.id.text_dock_export);
        TextView textDockImport = dialogView.findViewById(R.id.text_dock_import);
        ImageView iconDockExport = dialogView.findViewById(R.id.icon_dock_export);
        ImageView iconDockImport = dialogView.findViewById(R.id.icon_dock_import);
        TextView iconDockPlus = dialogView.findViewById(R.id.icon_dock_plus);

        // Apply Theme Backgrounds and Colors (Liquid Island Tokens)
        if (bookmarksRoot != null) bookmarksRoot.setBackgroundColor(dark.getAsBoolean() ? 0xFF070A10 : 0xFFF8FAFC);
        if (headerBar != null) headerBar.setBackgroundColor(dark.getAsBoolean() ? 0xFF070A10 : 0xFFF8FAFC);
        if (btnBack != null) btnBack.setColorFilter(dark.getAsBoolean() ? 0xFFBAC9CC : 0xFF334155);

        // Header Capsule: Liquid Glass squircle with Cyan glow
        if (capsuleCenter != null) {
            GradientDrawable capBg = new GradientDrawable();
            capBg.setCornerRadius(dpToPx(16));
            capBg.setColor(dark.getAsBoolean() ? 0xD0121826 : 0xE6FFFFFF);
            capBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);
            capsuleCenter.setBackground(capBg);
        }
        if (textBookmarksHeader != null) textBookmarksHeader.setTextColor(dark.getAsBoolean() ? 0xFFFFFFFF : 0xFF0284C7);
        if (iconBookmarksHeader != null) iconBookmarksHeader.setColorFilter(dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);

        if (badgeHeaderCount != null) {
            GradientDrawable bBg = new GradientDrawable();
            bBg.setCornerRadius(dpToPx(10));
            bBg.setColor(dark.getAsBoolean() ? 0x3322D3EE : 0x180284C7);
            bBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x5522D3EE : 0x300284C7);
            badgeHeaderCount.setBackground(bBg);
            badgeHeaderCount.setTextColor(dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);
        }

        if (btnGotoHistory != null) btnGotoHistory.setTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF475569);
        if (btnGotoSettings != null) btnGotoSettings.setTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF475569);

        if (textVaultTitle != null) textVaultTitle.setTextColor(dark.getAsBoolean() ? 0xFFF8FAFC : 0xFF0F172A);
        if (badgeVaultSaved != null) {
            GradientDrawable vBg = new GradientDrawable();
            vBg.setCornerRadius(dpToPx(8));
            vBg.setColor(dark.getAsBoolean() ? 0x2822D3EE : 0x180284C7);
            vBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x4422D3EE : 0x300284C7);
            badgeVaultSaved.setBackground(vBg);
            badgeVaultSaved.setTextColor(dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);
        }

        if (btnBatchToggle != null) {
            GradientDrawable btBg = new GradientDrawable();
            btBg.setCornerRadius(dpToPx(12));
            btBg.setColor(dark.getAsBoolean() ? 0x20FFFFFF : 0x0C0F172A);
            btBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x28FFFFFF : 0x140F172A);
            btnBatchToggle.setBackground(btBg);
            btnBatchToggle.setTextColor(dark.getAsBoolean() ? 0xFFE2E8F0 : 0xFF334155);
        }

        // Search Bar: Frosted Glass Capsule with Specular Border
        if (searchBar != null) {
            GradientDrawable sbBg = new GradientDrawable();
            sbBg.setCornerRadius(dpToPx(16));
            sbBg.setColor(dark.getAsBoolean() ? 0xD0121826 : 0xF0FFFFFF);
            sbBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x22FFFFFF : 0x140F172A);
            searchBar.setBackground(sbBg);
        }
        if (iconSearch != null) iconSearch.setColorFilter(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
        if (searchInput != null) {
            searchInput.setTextColor(dark.getAsBoolean() ? 0xFFF8FAFC : 0xFF0F172A);
            searchInput.setHintTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF94A3B8);
        }

        if (btnSort != null) {
            GradientDrawable sBg = new GradientDrawable();
            sBg.setCornerRadius(dpToPx(10));
            sBg.setColor(dark.getAsBoolean() ? 0x20FFFFFF : 0x0C0F172A);
            sBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x28FFFFFF : 0x140F172A);
            btnSort.setBackground(sBg);
        }
        if (textSortLabel != null) textSortLabel.setTextColor(dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);

        // Bottom Dock: HyperGlass Pill with Floating Cyan Glow FAB
        if (bottomDock != null) {
            GradientDrawable dockBg = new GradientDrawable();
            dockBg.setCornerRadius(dpToPx(26));
            dockBg.setColor(dark.getAsBoolean() ? 0xEE121826 : 0xF5FFFFFF);
            dockBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x33FFFFFF : 0x1E0F172A);
            bottomDock.setBackground(dockBg);
            bottomDock.setElevation(dpToPx(24));
        }
        if (textDockExport != null) textDockExport.setTextColor(dark.getAsBoolean() ? 0xFFF8FAFC : 0xFF0F172A);
        if (textDockImport != null) textDockImport.setTextColor(dark.getAsBoolean() ? 0xFFF8FAFC : 0xFF0F172A);
        if (iconDockExport != null) iconDockExport.setColorFilter(dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);
        if (iconDockImport != null) iconDockImport.setColorFilter(dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);

        if (btnDockAdd != null) {
            GradientDrawable plusBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF22D3EE, 0xFF06B6D4}
            );
            plusBg.setShape(GradientDrawable.OVAL);
            btnDockAdd.setBackground(plusBg);
        }
        if (iconDockPlus != null) {
            iconDockPlus.setTextColor(0xFF070A10);
        }

        // State variables
        final boolean[] isSelectMode = {false};
        final Set<String> selectedBookmarkIds = new HashSet<>();
        final String[] activeCategory = {"all"}; // "all", "favorites", or custom folder name
        final String[] activeSort = {"newest"}; // "newest", "oldest", "az", "za"

        // References for dynamic refresh
        final Runnable[] refreshRef = new Runnable[1];
        final Runnable[] renderChipsRef = new Runnable[1];

        // Dynamic Group Chips Renderer
        renderChipsRef[0] = () -> {
            if (layoutChips == null) return;
            while (layoutChips.getChildCount() > 2) {
                layoutChips.removeViewAt(2);
            }

            // Style static chips (All, Favorites)
            boolean isAllAct = "all".equalsIgnoreCase(activeCategory[0]);
            GradientDrawable allBg = new GradientDrawable();
            allBg.setCornerRadius(dpToPx(14));
            allBg.setColor(isAllAct ? (dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7) : (dark.getAsBoolean() ? 0x1CFFFFFF : 0x0C0F172A));
            if (!isAllAct) allBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x24FFFFFF : 0x140F172A);
            if (chipAll != null) {
                chipAll.setBackground(allBg);
                chipAll.setTextColor(isAllAct ? (dark.getAsBoolean() ? 0xFF070A10 : 0xFFFFFFFF) : (dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B));
            }

            boolean isFavAct = "favorites".equalsIgnoreCase(activeCategory[0]);
            GradientDrawable favBg = new GradientDrawable();
            favBg.setCornerRadius(dpToPx(14));
            favBg.setColor(isFavAct ? (dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7) : (dark.getAsBoolean() ? 0x1CFFFFFF : 0x0C0F172A));
            if (!isFavAct) favBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x24FFFFFF : 0x140F172A);
            if (chipFav != null) {
                chipFav.setBackground(favBg);
                chipFav.setTextColor(isFavAct ? (dark.getAsBoolean() ? 0xFF070A10 : 0xFFFFFFFF) : (dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B));
            }

            // Dynamic Custom Groups Chips
            List<String> folders = bookmarkManager.getFolders();
            for (String folder : folders) {
                if ("Favorites".equalsIgnoreCase(folder) || "All".equalsIgnoreCase(folder) || "Default".equalsIgnoreCase(folder)) {
                    continue;
                }
                TextView chip = new TextView(activity);
                chip.setText(folder.toUpperCase(Locale.ROOT));
                chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                chip.setTypeface(null, Typeface.BOLD);
                chip.setGravity(Gravity.CENTER);
                chip.setPadding(dpToPx(12), 0, dpToPx(12), 0);
                chip.setClickable(true);
                chip.setFocusable(true);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, dpToPx(28));
                lp.setMarginStart(dpToPx(6));
                chip.setLayoutParams(lp);

                boolean isAct = folder.equalsIgnoreCase(activeCategory[0]);
                GradientDrawable cBg = new GradientDrawable();
                cBg.setCornerRadius(dpToPx(14));
                cBg.setColor(isAct ? (dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7) : (dark.getAsBoolean() ? 0x1CFFFFFF : 0x0C0F172A));
                if (!isAct) cBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x24FFFFFF : 0x140F172A);
                chip.setBackground(cBg);
                chip.setTextColor(isAct ? (dark.getAsBoolean() ? 0xFF070A10 : 0xFFFFFFFF) : (dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B));

                chip.setOnClickListener(v -> {
                    sound.accept("tap");
                    activeCategory[0] = folder;
                    renderChipsRef[0].run();
                    if (refreshRef[0] != null) refreshRef[0].run();
                });

                // Long-press: Rename or Delete Bookmark Group
                chip.setOnLongClickListener(v -> {
                    sound.accept("tap");
                    PopupMenu groupMenu = new PopupMenu(activity, chip);
                    groupMenu.getMenu().add(0, 1, 0, "✏️ Rename Group");
                    groupMenu.getMenu().add(0, 2, 1, "🗑️ Delete Group");
                    groupMenu.setOnMenuItemClickListener(item -> {
                        if (item.getItemId() == 1) {
                            showRenameGroupDialog(folder, () -> {
                                activeCategory[0] = "all";
                                renderChipsRef[0].run();
                                if (refreshRef[0] != null) refreshRef[0].run();
                            });
                            return true;
                        } else if (item.getItemId() == 2) {
                            showDeleteGroupDialog(folder, () -> {
                                activeCategory[0] = "all";
                                renderChipsRef[0].run();
                                if (refreshRef[0] != null) refreshRef[0].run();
                            });
                            return true;
                        }
                        return false;
                    });
                    groupMenu.show();
                    return true;
                });

                layoutChips.addView(chip);
            }
        };

        // Static chip click listeners
        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                sound.accept("tap");
                activeCategory[0] = "all";
                renderChipsRef[0].run();
                if (refreshRef[0] != null) refreshRef[0].run();
            });
        }
        if (chipFav != null) {
            chipFav.setOnClickListener(v -> {
                sound.accept("tap");
                activeCategory[0] = "favorites";
                renderChipsRef[0].run();
                if (refreshRef[0] != null) refreshRef[0].run();
            });
        }

        // Header Navigation Listeners with Smooth Slide Exit Animations
        if (btnBack != null) btnBack.setOnClickListener(v -> dialog.dismiss());
        if (btnGotoHistory != null) {
            btnGotoHistory.setOnClickListener(v -> {
                sound.accept("tap");
                dialog.dismiss();
                showHistoryDialog(1);
            });
        }
        if (btnGotoSettings != null) {
            btnGotoSettings.setOnClickListener(v -> {
                sound.accept("tap");
                dialog.dismiss();
                activity.openBrowserSettings();
            });
        }

        // Bookmark List Refresh Logic
        refreshRef[0] = () -> {
            if (listContainer == null) return;
            listContainer.removeAllViews();
            List<BookmarkManager.BookmarkItem> bms = bookmarkManager.getAllBookmarks();

            // Total count badges
            if (badgeHeaderCount != null) badgeHeaderCount.setText(String.valueOf(bms.size()));
            if (badgeVaultSaved != null) badgeVaultSaved.setText(bms.size() + " SAVED");
            if (chipAll != null) chipAll.setText("ALL (" + bms.size() + ")");

            // Filter
            String query = searchInput != null ? searchInput.getText().toString().trim().toLowerCase() : "";
            List<BookmarkManager.BookmarkItem> filtered = new ArrayList<>();
            for (BookmarkManager.BookmarkItem b : bms) {
                if (activeCategory[0].equalsIgnoreCase("favorites") && !b.isFavorite) continue;
                if (!activeCategory[0].equalsIgnoreCase("all") && !activeCategory[0].equalsIgnoreCase("favorites")) {
                    if (b.folder == null || !b.folder.equalsIgnoreCase(activeCategory[0])) continue;
                }

                if (!query.isEmpty()) {
                    boolean match = (b.title != null && b.title.toLowerCase().contains(query))
                            || (b.url != null && b.url.toLowerCase().contains(query))
                            || (b.folder != null && b.folder.toLowerCase().contains(query));
                    if (!match) continue;
                }
                filtered.add(b);
            }

            // Sort
            if ("newest".equals(activeSort[0])) {
                Collections.sort(filtered, (a, b) -> Long.compare(b.timestamp, a.timestamp));
            } else if ("oldest".equals(activeSort[0])) {
                Collections.sort(filtered, (a, b) -> Long.compare(a.timestamp, b.timestamp));
            } else if ("az".equals(activeSort[0])) {
                Collections.sort(filtered, (a, b) -> a.title.compareToIgnoreCase(b.title));
            } else if ("za".equals(activeSort[0])) {
                Collections.sort(filtered, (a, b) -> b.title.compareToIgnoreCase(a.title));
            }

            if (emptyView != null) emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);

            // Render each bookmark using item_stitch_bookmark_entry.xml
            for (BookmarkManager.BookmarkItem item : filtered) {
                View card = activity.getLayoutInflater().inflate(R.layout.item_stitch_bookmark_entry, listContainer, false);
                LinearLayout itemRoot = card.findViewById(R.id.bookmark_item_root);
                android.widget.CheckBox itemCheck = card.findViewById(R.id.bookmark_item_checkbox);
                FrameLayout iconFrame = card.findViewById(R.id.bookmark_item_icon_frame);
                TextView iconEmoji = card.findViewById(R.id.bookmark_item_emoji);
                TextView titleView = card.findViewById(R.id.bookmark_item_title);
                TextView tagView = card.findViewById(R.id.bookmark_item_tag);
                TextView domainView = card.findViewById(R.id.bookmark_item_domain);
                TextView timeView = card.findViewById(R.id.bookmark_item_time);
                ImageButton starBtn = card.findViewById(R.id.bookmark_item_star);
                ImageButton moreBtn = card.findViewById(R.id.bookmark_item_more);

                // Card Background (Liquid Island Glass)
                if (itemRoot != null) {
                    GradientDrawable cBg = new GradientDrawable();
                    cBg.setCornerRadius(dpToPx(18));
                    cBg.setColor(dark.getAsBoolean() ? 0xD0121826 : 0xF2FFFFFF);
                    cBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x22FFFFFF : 0x120F172A);
                    itemRoot.setBackground(cBg);
                }

                // Batch Selection Checkbox
                if (itemCheck != null) {
                    itemCheck.setVisibility(isSelectMode[0] ? View.VISIBLE : View.GONE);
                    itemCheck.setChecked(selectedBookmarkIds.contains(item.id));
                    itemCheck.setOnCheckedChangeListener((cb, checked) -> {
                        if (checked) selectedBookmarkIds.add(item.id);
                        else selectedBookmarkIds.remove(item.id);
                        if (btnBatchDelete != null) {
                            btnBatchDelete.setVisibility(!selectedBookmarkIds.isEmpty() ? View.VISIBLE : View.GONE);
                            btnBatchDelete.setText("Delete (" + selectedBookmarkIds.size() + ")");
                        }
                    });
                }

                // Title & Tag
                if (titleView != null) {
                    titleView.setText(item.title);
                    titleView.setTextColor(dark.getAsBoolean() ? 0xFFF8FAFC : 0xFF0F172A);
                }
                if (tagView != null) {
                    if (item.folder != null && !item.folder.isEmpty() && !"Default".equalsIgnoreCase(item.folder)) {
                        tagView.setVisibility(View.VISIBLE);
                        String tagShort = item.folder.contains("&") ? item.folder.split("&")[0].trim() : item.folder;
                        if (tagShort.length() > 8) tagShort = tagShort.substring(0, 8);
                        tagView.setText(tagShort.toUpperCase());
                        GradientDrawable tBg = new GradientDrawable();
                        tBg.setCornerRadius(dpToPx(6));
                        tBg.setColor(dark.getAsBoolean() ? 0x2822D3EE : 0x180284C7);
                        tBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x4422D3EE : 0x300284C7);
                        tagView.setBackground(tBg);
                        tagView.setTextColor(dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);
                    } else {
                        tagView.setVisibility(View.GONE);
                    }
                }

                // Domain & Relative Time
                if (domainView != null) {
                    String domain = item.url;
                    try {
                        Uri u = Uri.parse(item.url);
                        if (u != null && u.getHost() != null) domain = u.getHost();
                    } catch (Exception ignored) {}
                    domainView.setText(domain);
                    domainView.setTextColor(dark.getAsBoolean() ? 0xFF849396 : 0xFF64748B);
                }

                if (timeView != null) {
                    long diff = System.currentTimeMillis() - item.timestamp;
                    String timeStr;
                    if (diff < 60000L) timeStr = "just now";
                    else if (diff < 3600000L) timeStr = (diff / 60000L) + "m ago";
                    else if (diff < 86400000L) timeStr = (diff / 3600000L) + "h ago";
                    else if (diff < 604800000L) timeStr = (diff / 86400000L) + "d ago";
                    else if (diff < 2592000000L) timeStr = (diff / 604800000L) + "w ago";
                    else timeStr = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.US).format(new java.util.Date(item.timestamp));
                    timeView.setText(timeStr);
                    timeView.setTextColor(dark.getAsBoolean() ? 0xFF10B981 : 0xFF059669);
                }

                // Squircle Icon Frame
                if (iconFrame != null && iconEmoji != null) {
                    String u = item.url.toLowerCase();
                    String t = item.title.toLowerCase();
                    int squircleBg;
                    String emoji;
                    if (u.contains("notion") || t.contains("notion") || t.contains("spec") || t.contains("doc")) {
                        squircleBg = dark.getAsBoolean() ? 0xFF064E3B : 0xFFD1FAE5;
                        emoji = "📝";
                    } else if (u.contains("arxiv") || t.contains("ai") || t.contains("llm") || t.contains("prompt")) {
                        squircleBg = dark.getAsBoolean() ? 0xFF78350F : 0xFFFEF3C7;
                        emoji = "📖";
                    } else if (u.contains("figma") || t.contains("figma") || t.contains("design") || t.contains("token")) {
                        squircleBg = dark.getAsBoolean() ? 0xFF831843 : 0xFFFCE7F3;
                        emoji = "✏️";
                    } else if (u.contains("tailwind") || u.contains("github") || t.contains("code") || t.contains("dev")) {
                        squircleBg = dark.getAsBoolean() ? 0xFF164E63 : 0xFFCFFAFE;
                        emoji = "💻";
                    } else if (u.contains("chatgpt") || u.contains("gemini") || u.contains("claude")) {
                        squircleBg = dark.getAsBoolean() ? 0xFF312E81 : 0xFFEDE9FE;
                        emoji = "🤖";
                    } else {
                        squircleBg = dark.getAsBoolean() ? 0xFF1E293B : 0xFFF1F5F9;
                        emoji = "🔖";
                    }

                    GradientDrawable fBg = new GradientDrawable();
                    fBg.setCornerRadius(dpToPx(12));
                    fBg.setColor(squircleBg);
                    iconFrame.setBackground(fBg);
                    iconEmoji.setText(emoji);
                }

                // Favorite Star Button
                if (starBtn != null) {
                    starBtn.setImageResource(R.drawable.ic_menu_bookmark);
                    starBtn.setColorFilter(item.isFavorite ? 0xFFFBBF24 : (dark.getAsBoolean() ? 0xFF475569 : 0xFFCBD5E1));
                    starBtn.setOnClickListener(v -> {
                        sound.accept("tap");
                        bookmarkManager.toggleFavorite(item.id);
                        refreshRef[0].run();
                    });
                }

                // Pencil Button: Options (Move to group, Edit, Tabs, Copy, Delete)
                if (moreBtn != null) {
                    moreBtn.setColorFilter(dark.getAsBoolean() ? 0xFF849396 : 0xFF64748B);
                    moreBtn.setOnClickListener(v -> {
                        sound.accept("tap");
                        PopupMenu popup = new PopupMenu(activity, moreBtn);
                        popup.getMenu().add(0, 1, 0, "📁 Move to Group");
                        popup.getMenu().add(0, 2, 1, "✏️ Edit Bookmark");
                        popup.getMenu().add(0, 3, 2, "Open in Active Tab");
                        popup.getMenu().add(0, 4, 3, "Open in New Tab");
                        popup.getMenu().add(0, 5, 4, "Copy Link");
                        popup.getMenu().add(0, 6, 5, "Delete Bookmark");
                        popup.setOnMenuItemClickListener(mi -> {
                            if (mi.getItemId() == 1) {
                                showMoveBookmarkToGroupDialog(item, refreshRef[0], renderChipsRef[0]);
                                return true;
                            } else if (mi.getItemId() == 2) {
                                showEditBookmarkDialog(item, refreshRef[0], renderChipsRef[0]);
                                return true;
                            } else if (mi.getItemId() == 3) {
                                dialog.dismiss();
                                TabItem active = activeTab.get();
                                if (active != null && active.webView != null) active.webView.loadUrl(item.url);
                                else activity.addNewTab("web", null, item.url, false);
                                return true;
                            } else if (mi.getItemId() == 4) {
                                dialog.dismiss();
                                activity.addNewTab("web", null, item.url, false);
                                return true;
                            } else if (mi.getItemId() == 5) {
                                ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                                if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("URL", item.url));
                                Toast.makeText(activity, "Link copied to clipboard", Toast.LENGTH_SHORT).show();
                                return true;
                            } else if (mi.getItemId() == 6) {
                                bookmarkManager.deleteBookmark(item.id);
                                Toast.makeText(activity, "Bookmark deleted", Toast.LENGTH_SHORT).show();
                                refreshRef[0].run();
                                return true;
                            }
                            return false;
                        });
                        popup.show();
                    });
                }

                // Card Click
                card.setOnClickListener(v -> {
                    if (isSelectMode[0]) {
                        if (itemCheck != null) itemCheck.toggle();
                    } else {
                        dialog.dismiss();
                        sound.accept("tap");
                        TabItem active = activeTab.get();
                        if (active != null && active.webView != null) active.webView.loadUrl(item.url);
                        else activity.addNewTab("web", null, item.url, false);
                    }
                });

                listContainer.addView(card);
            }
        };

        currentBookmarksRefreshRunnable = refreshRef[0];

        // Batch Select Toggle Listener
        if (btnBatchToggle != null) {
            btnBatchToggle.setOnClickListener(v -> {
                sound.accept("tap");
                isSelectMode[0] = !isSelectMode[0];
                btnBatchToggle.setText(isSelectMode[0] ? "CANCEL" : "SELECT");
                selectedBookmarkIds.clear();
                if (btnBatchDelete != null) btnBatchDelete.setVisibility(View.GONE);
                refreshRef[0].run();
            });
        }

        // Batch Delete Listener
        if (btnBatchDelete != null) {
            btnBatchDelete.setOnClickListener(v -> {
                sound.accept("tap");
                if (!selectedBookmarkIds.isEmpty()) {
                    new AlertDialog.Builder(activity)
                            .setTitle("Delete Bookmarks")
                            .setMessage("Delete " + selectedBookmarkIds.size() + " selected bookmarks?")
                            .setPositiveButton("Delete", (d, which) -> {
                                bookmarkManager.batchDelete(new ArrayList<>(selectedBookmarkIds));
                                selectedBookmarkIds.clear();
                                isSelectMode[0] = false;
                                btnBatchToggle.setText("SELECT");
                                btnBatchDelete.setVisibility(View.GONE);
                                refreshRef[0].run();
                                Toast.makeText(activity, "Bookmarks deleted", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });
        }

        // Sort Button Listener
        if (btnSort != null) {
            btnSort.setOnClickListener(v -> {
                sound.accept("tap");
                PopupMenu sortMenu = new PopupMenu(activity, btnSort);
                sortMenu.getMenu().add(0, 1, 0, "Newest First");
                sortMenu.getMenu().add(0, 2, 1, "Oldest First");
                sortMenu.getMenu().add(0, 3, 2, "Title (A-Z)");
                sortMenu.getMenu().add(0, 4, 3, "Title (Z-A)");
                sortMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) { activeSort[0] = "newest"; textSortLabel.setText("Newest"); }
                    else if (item.getItemId() == 2) { activeSort[0] = "oldest"; textSortLabel.setText("Oldest"); }
                    else if (item.getItemId() == 3) { activeSort[0] = "az"; textSortLabel.setText("A-Z"); }
                    else if (item.getItemId() == 4) { activeSort[0] = "za"; textSortLabel.setText("Z-A"); }
                    refreshRef[0].run();
                    return true;
                });
                sortMenu.show();
            });
        }

        // Search Input Listener
        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshRef[0].run(); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Bottom Dock: Export Bookmarks Button
        if (btnDockExport != null) {
            btnDockExport.setOnClickListener(v -> {
                sound.accept("tap");
                try {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    activity.startActivityForResult(intent, REQUEST_CODE_EXPORT_BOOKMARKS_TREE);
                    Toast.makeText(activity, "Select export destination folder", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(activity, "Export unavailable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Bottom Dock: Import Bookmarks Button
        if (btnDockImport != null) {
            btnDockImport.setOnClickListener(v -> {
                sound.accept("tap");
                try {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    activity.startActivityForResult(Intent.createChooser(intent, "Select Bookmarks File (HTML/JSON)"), REQUEST_CODE_IMPORT_BOOKMARKS_FILE);
                } catch (Exception e) {
                    Toast.makeText(activity, "Import unavailable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Bottom Dock: Add Bookmark or Group Plus Button (Presents Action Menu)
        if (btnDockAdd != null) {
            btnDockAdd.setOnClickListener(v -> {
                sound.accept("tap");
                showAddBookmarkOrGroupMenu(() -> {
                    renderChipsRef[0].run();
                    refreshRef[0].run();
                });
            });
        }

        renderChipsRef[0].run();
        refreshRef[0].run();
        dialog.show();
    }

    private void showAddBookmarkOrGroupMenu(Runnable onRefresh) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(24), dpToPx(22), dpToPx(24), dpToPx(20));

        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setCornerRadius(dpToPx(24));
        rootBg.setColor(dark.getAsBoolean() ? 0xFF141926 : 0xFFFFFFFF);
        rootBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF1F293D : 0xFFCBD5E1);
        root.setBackground(rootBg);

        TextView title = new TextView(activity);
        title.setText("Add to Caspian Vault");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(dark.getAsBoolean() ? 0xFFFFFFFF : 0xFF0F172A);
        root.addView(title);

        TextView subtitle = new TextView(activity);
        subtitle.setText("Choose what you would like to create");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        subtitle.setTextColor(dark.getAsBoolean() ? 0xFF849396 : 0xFF64748B);
        subtitle.setPadding(0, dpToPx(4), 0, dpToPx(18));
        root.addView(subtitle);

        AlertDialog menuDialog = builder.setView(root).create();
        if (menuDialog.getWindow() != null) {
            menuDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Option 1: New Bookmark
        LinearLayout cardBookmark = createVaultMenuOptionCard(
                "🔖", "New Bookmark", "Save active page or enter a custom link",
                () -> {
                    menuDialog.dismiss();
                    showAddBookmarkDialog(onRefresh);
                }
        );
        root.addView(cardBookmark);

        // Option 2: New Bookmark Group
        LinearLayout cardGroup = createVaultMenuOptionCard(
                "📁", "New Bookmark Group", "Create a custom category folder for bookmarks",
                () -> {
                    menuDialog.dismiss();
                    showCreateBookmarkGroupDialog(onRefresh);
                }
        );
        root.addView(cardGroup);

        menuDialog.show();
    }

    private LinearLayout createVaultMenuOptionCard(String emoji, String titleText, String subtitleText, Runnable onClick) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dpToPx(14), dpToPx(14), dpToPx(14), dpToPx(14));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dpToPx(10);
        card.setLayoutParams(lp);

        GradientDrawable cBg = new GradientDrawable();
        cBg.setCornerRadius(dpToPx(16));
        cBg.setColor(dark.getAsBoolean() ? 0xFF1B2232 : 0xFFF8FAFC);
        cBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF27334D : 0xFFE2E8F0);
        card.setBackground(cBg);

        // Emoji Frame
        FrameLayout iconFrame = new FrameLayout(activity);
        LinearLayout.LayoutParams ifLp = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
        ifLp.setMarginEnd(dpToPx(12));
        iconFrame.setLayoutParams(ifLp);

        GradientDrawable ifBg = new GradientDrawable();
        ifBg.setCornerRadius(dpToPx(12));
        ifBg.setColor(dark.getAsBoolean() ? 0xFF141926 : 0xFFEEF2F6);
        iconFrame.setBackground(ifBg);

        TextView tvEmoji = new TextView(activity);
        tvEmoji.setText(emoji);
        tvEmoji.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        tvEmoji.setGravity(Gravity.CENTER);
        iconFrame.addView(tvEmoji);
        card.addView(iconFrame);

        // Text details Column
        LinearLayout col = new LinearLayout(activity);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvTitle = new TextView(activity);
        tvTitle.setText(titleText);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(dark.getAsBoolean() ? 0xFFF1F5F9 : 0xFF0F172A);
        col.addView(tvTitle);

        TextView tvSub = new TextView(activity);
        tvSub.setText(subtitleText);
        tvSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        tvSub.setTextColor(dark.getAsBoolean() ? 0xFF849396 : 0xFF64748B);
        tvSub.setPadding(0, dpToPx(2), 0, 0);
        col.addView(tvSub);

        card.addView(col);

        // Right arrow
        TextView arrow = new TextView(activity);
        arrow.setText("›");
        arrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f);
        arrow.setTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF94A3B8);
        card.addView(arrow);

        card.setOnClickListener(v -> {
            sound.accept("tap");
            if (onClick != null) onClick.run();
        });

        return card;
    }

    private void showMoveBookmarkToGroupDialog(BookmarkManager.BookmarkItem item, Runnable onRefresh, Runnable onChipsRefresh) {
        if (item == null || bookmarkManager == null) return;
        sound.accept("tap");
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(24), dpToPx(22), dpToPx(24), dpToPx(20));

        GradientDrawable rBg = new GradientDrawable();
        rBg.setCornerRadius(dpToPx(22));
        rBg.setColor(dark.getAsBoolean() ? 0xFF141926 : 0xFFFFFFFF);
        rBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF232B3E : 0xFFCBD5E1);
        root.setBackground(rBg);

        TextView title = new TextView(activity);
        title.setText("Move to Group");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(dark.getAsBoolean() ? 0xFFFFFFFF : 0xFF0F172A);
        root.addView(title);

        TextView sub = new TextView(activity);
        sub.setText("Select destination group for: \"" + item.title + "\"");
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        sub.setTextColor(dark.getAsBoolean() ? 0xFF849396 : 0xFF64748B);
        sub.setPadding(0, dpToPx(4), 0, dpToPx(14));
        root.addView(sub);

        List<String> folderList = new ArrayList<>();
        folderList.add("Default");
        List<String> existing = bookmarkManager.getFolders();
        for (String f : existing) {
            if (!f.equalsIgnoreCase("Favorites") && !f.equalsIgnoreCase("All") && !f.equalsIgnoreCase("Default")) {
                folderList.add(f);
            }
        }

        AlertDialog moveDialog = builder.setView(root).create();
        if (moveDialog.getWindow() != null) {
            moveDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        for (String folder : folderList) {
            TextView opt = new TextView(activity);
            boolean isCurrent = folder.equalsIgnoreCase(item.folder);
            opt.setText((isCurrent ? "✓ 📁 " : "📁 ") + folder + (isCurrent ? " (Current)" : ""));
            opt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
            opt.setTextColor(isCurrent ? 0xFF00E5FF : (dark.getAsBoolean() ? 0xFFDFE2F0 : 0xFF0F172A));
            opt.setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10));
            GradientDrawable optBg = new GradientDrawable();
            optBg.setCornerRadius(dpToPx(10));
            optBg.setColor(isCurrent ? (dark.getAsBoolean() ? 0xFF1B2A3D : 0xFFE0F2FE) : (dark.getAsBoolean() ? 0xFF19202E : 0xFFF1F5F9));
            opt.setBackground(optBg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dpToPx(6));
            opt.setLayoutParams(lp);
            opt.setOnClickListener(v -> {
                sound.accept("tap");
                bookmarkManager.moveBookmarkToFolder(item.id, folder);
                Toast.makeText(activity, "Moved to " + folder + "! 📁", Toast.LENGTH_SHORT).show();
                moveDialog.dismiss();
                if (onRefresh != null) onRefresh.run();
                if (onChipsRefresh != null) onChipsRefresh.run();
            });
            root.addView(opt);
        }

        // Add Create New Group & Move Here
        TextView optNew = new TextView(activity);
        optNew.setText("➕ Create New Group & Move Here...");
        optNew.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        optNew.setTypeface(null, Typeface.BOLD);
        optNew.setTextColor(0xFF00E5FF);
        optNew.setPadding(dpToPx(14), dpToPx(10), dpToPx(14), dpToPx(10));
        GradientDrawable nBg = new GradientDrawable();
        nBg.setCornerRadius(dpToPx(10));
        nBg.setColor(dark.getAsBoolean() ? 0xFF122030 : 0xFFE0F2FE);
        nBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF00E5FF : 0xFF0284C7);
        optNew.setBackground(nBg);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.setMargins(0, dpToPx(4), 0, dpToPx(12));
        optNew.setLayoutParams(nlp);
        optNew.setOnClickListener(v -> {
            sound.accept("tap");
            moveDialog.dismiss();
            showCreateBookmarkGroupDialog(() -> {
                List<String> updated = bookmarkManager.getFolders();
                if (!updated.isEmpty()) {
                    String newest = updated.get(updated.size() - 1);
                    bookmarkManager.moveBookmarkToFolder(item.id, newest);
                    Toast.makeText(activity, "Moved to " + newest + "! 📁", Toast.LENGTH_SHORT).show();
                }
                if (onRefresh != null) onRefresh.run();
                if (onChipsRefresh != null) onChipsRefresh.run();
            });
        });
        root.addView(optNew);

        TextView btnCancel = new TextView(activity);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnCancel.setTextColor(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
        btnCancel.setGravity(Gravity.CENTER);
        btnCancel.setPadding(0, dpToPx(6), 0, 0);
        btnCancel.setOnClickListener(v -> moveDialog.dismiss());
        root.addView(btnCancel);

        moveDialog.show();
    }

    private void showEditBookmarkDialog(BookmarkManager.BookmarkItem item, Runnable onRefresh, Runnable onChipsRefresh) {
        if (item == null || bookmarkManager == null) return;
        sound.accept("tap");
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(24), dpToPx(22), dpToPx(24), dpToPx(18));

        GradientDrawable rBg = new GradientDrawable();
        rBg.setCornerRadius(dpToPx(22));
        rBg.setColor(dark.getAsBoolean() ? 0xFF141926 : 0xFFFFFFFF);
        rBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF232B3E : 0xFFCBD5E1);
        root.setBackground(rBg);

        TextView title = new TextView(activity);
        title.setText("Edit Bookmark");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(dark.getAsBoolean() ? 0xFFFFFFFF : 0xFF0F172A);
        root.addView(title);

        // Title Input
        TextView lblTitle = new TextView(activity);
        lblTitle.setText("PAGE TITLE");
        lblTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblTitle.setTypeface(null, Typeface.BOLD);
        lblTitle.setTextColor(dark.getAsBoolean() ? 0xFF849396 : 0xFF64748B);
        lblTitle.setPadding(0, dpToPx(14), 0, dpToPx(4));
        root.addView(lblTitle);

        EditText etTitle = new EditText(activity);
        etTitle.setText(item.title);
        etTitle.setTextColor(dark.getAsBoolean() ? 0xFFDFE2F0 : 0xFF0F172A);
        etTitle.setHintTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF94A3B8);
        etTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        root.addView(etTitle);

        // URL Input
        TextView lblUrl = new TextView(activity);
        lblUrl.setText("WEB URL");
        lblUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblUrl.setTypeface(null, Typeface.BOLD);
        lblUrl.setTextColor(dark.getAsBoolean() ? 0xFF849396 : 0xFF64748B);
        lblUrl.setPadding(0, dpToPx(10), 0, dpToPx(4));
        root.addView(lblUrl);

        EditText etUrl = new EditText(activity);
        etUrl.setText(item.url);
        etUrl.setTextColor(dark.getAsBoolean() ? 0xFFDFE2F0 : 0xFF0F172A);
        etUrl.setHintTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF94A3B8);
        etUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        root.addView(etUrl);

        // Group Selector
        TextView lblGroup = new TextView(activity);
        lblGroup.setText("BOOKMARK GROUP");
        lblGroup.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblGroup.setTypeface(null, Typeface.BOLD);
        lblGroup.setTextColor(dark.getAsBoolean() ? 0xFF849396 : 0xFF64748B);
        lblGroup.setPadding(0, dpToPx(10), 0, dpToPx(4));
        root.addView(lblGroup);

        List<String> folderList = new ArrayList<>();
        folderList.add("Default");
        List<String> existing = bookmarkManager.getFolders();
        for (String f : existing) {
            if (!f.equalsIgnoreCase("Favorites") && !f.equalsIgnoreCase("All") && !f.equalsIgnoreCase("Default")) {
                folderList.add(f);
            }
        }
        final String[] selectedFolder = { (item.folder != null && !item.folder.isEmpty()) ? item.folder : "Default" };

        TextView btnGroupPicker = new TextView(activity);
        btnGroupPicker.setText("📁 " + selectedFolder[0]);
        btnGroupPicker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnGroupPicker.setTextColor(dark.getAsBoolean() ? 0xFF00E5FF : 0xFF0284C7);
        btnGroupPicker.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));

        GradientDrawable gpBg = new GradientDrawable();
        gpBg.setCornerRadius(dpToPx(10));
        gpBg.setColor(dark.getAsBoolean() ? 0xFF1E2838 : 0xFFE0F2FE);
        gpBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF2B3A50 : 0xFFBAE6FD);
        btnGroupPicker.setBackground(gpBg);
        btnGroupPicker.setClickable(true);
        btnGroupPicker.setFocusable(true);
        btnGroupPicker.setOnClickListener(v -> {
            sound.accept("tap");
            PopupMenu pm = new PopupMenu(activity, btnGroupPicker);
            for (int i = 0; i < folderList.size(); i++) {
                pm.getMenu().add(0, i, i, "📁 " + folderList.get(i));
            }
            pm.getMenu().add(0, folderList.size(), folderList.size(), "➕ New Group...");
            pm.setOnMenuItemClickListener(mi -> {
                int id = mi.getItemId();
                if (id >= 0 && id < folderList.size()) {
                    selectedFolder[0] = folderList.get(id);
                    btnGroupPicker.setText("📁 " + selectedFolder[0]);
                } else if (id == folderList.size()) {
                    showCreateBookmarkGroupDialog(() -> {
                        List<String> up = bookmarkManager.getFolders();
                        if (!up.isEmpty()) {
                            selectedFolder[0] = up.get(up.size() - 1);
                            btnGroupPicker.setText("📁 " + selectedFolder[0]);
                        }
                    });
                }
                return true;
            });
            pm.show();
        });
        root.addView(btnGroupPicker);

        AlertDialog editDialog = builder.setView(root).create();
        if (editDialog.getWindow() != null) {
            editDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout btnRow = new LinearLayout(activity);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, dpToPx(20), 0, 0);

        TextView btnCancel = new TextView(activity);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnCancel.setTextColor(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
        btnCancel.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
        btnCancel.setOnClickListener(v -> editDialog.dismiss());
        btnRow.addView(btnCancel);

        TextView btnSave = new TextView(activity);
        btnSave.setText("Save Changes");
        btnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnSave.setTypeface(null, Typeface.BOLD);
        btnSave.setTextColor(0xFF000000);
        btnSave.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));
        GradientDrawable svBg = new GradientDrawable();
        svBg.setCornerRadius(dpToPx(10));
        svBg.setColor(0xFF00E5FF);
        btnSave.setBackground(svBg);
        btnSave.setOnClickListener(v -> {
            sound.accept("tap");
            String t = etTitle.getText().toString().trim();
            String u = etUrl.getText().toString().trim();
            if (!u.isEmpty()) {
                if (!u.startsWith("http://") && !u.startsWith("https://")) u = "https://" + u;
                if (t.isEmpty()) t = u;
                bookmarkManager.updateBookmark(item.id, t, u, selectedFolder[0]);
                Toast.makeText(activity, "Bookmark updated! ✨", Toast.LENGTH_SHORT).show();
                editDialog.dismiss();
                if (onRefresh != null) onRefresh.run();
                if (onChipsRefresh != null) onChipsRefresh.run();
            }
        });
        btnRow.addView(btnSave);
        root.addView(btnRow);

        editDialog.show();
    }

    public void showAddBookmarkDialog() {
        showAddBookmarkDialog(null);
    }

    private void showAddBookmarkDialog(Runnable onRefresh) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(24), dpToPx(22), dpToPx(24), dpToPx(18));

        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setCornerRadius(dpToPx(22));
        rootBg.setColor(dark.getAsBoolean() ? 0xFF141926 : 0xFFFFFFFF);
        rootBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF1F293D : 0xFFCBD5E1);
        root.setBackground(rootBg);

        TextView title = new TextView(activity);
        title.setText("Add Bookmark");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(dark.getAsBoolean() ? 0xFFFFFFFF : 0xFF0F172A);
        root.addView(title);

        MainActivity.TabItem tab = activeTab.get();

        // Title Input
        TextView lblTitle = new TextView(activity);
        lblTitle.setText("PAGE TITLE");
        lblTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblTitle.setTypeface(null, Typeface.BOLD);
        lblTitle.setTextColor(dark.getAsBoolean() ? 0xFF849396 : 0xFF64748B);
        lblTitle.setPadding(0, dpToPx(14), 0, dpToPx(4));
        root.addView(lblTitle);

        EditText etTitle = new EditText(activity);
        etTitle.setHint("Bookmark Title");
        if (tab != null && tab.title != null) etTitle.setText(tab.title);
        etTitle.setTextColor(dark.getAsBoolean() ? 0xFFDFE2F0 : 0xFF0F172A);
        etTitle.setHintTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF94A3B8);
        etTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        root.addView(etTitle);

        // URL Input
        TextView lblUrl = new TextView(activity);
        lblUrl.setText("WEB URL");
        lblUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblUrl.setTypeface(null, Typeface.BOLD);
        lblUrl.setTextColor(dark.getAsBoolean() ? 0xFF849396 : 0xFF64748B);
        lblUrl.setPadding(0, dpToPx(10), 0, dpToPx(4));
        root.addView(lblUrl);

        EditText etUrl = new EditText(activity);
        etUrl.setHint("https://...");
        if (tab != null && tab.url != null) etUrl.setText(tab.url);
        etUrl.setTextColor(dark.getAsBoolean() ? 0xFFDFE2F0 : 0xFF0F172A);
        etUrl.setHintTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF94A3B8);
        etUrl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        root.addView(etUrl);

        // Group Selector
        TextView lblGroup = new TextView(activity);
        lblGroup.setText("BOOKMARK GROUP");
        lblGroup.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblGroup.setTypeface(null, Typeface.BOLD);
        lblGroup.setTextColor(dark.getAsBoolean() ? 0xFF849396 : 0xFF64748B);
        lblGroup.setPadding(0, dpToPx(10), 0, dpToPx(4));
        root.addView(lblGroup);

        List<String> folderList = new ArrayList<>();
        folderList.add("Default");
        List<String> existing = bookmarkManager.getFolders();
        for (String f : existing) {
            if (!f.equalsIgnoreCase("Favorites") && !f.equalsIgnoreCase("All") && !f.equalsIgnoreCase("Default")) {
                folderList.add(f);
            }
        }
        final String[] selectedFolder = {folderList.get(0)};

        TextView btnGroupPicker = new TextView(activity);
        btnGroupPicker.setText("📁 " + selectedFolder[0]);
        btnGroupPicker.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnGroupPicker.setTextColor(dark.getAsBoolean() ? 0xFF00E5FF : 0xFF0284C7);
        btnGroupPicker.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));

        GradientDrawable gpBg = new GradientDrawable();
        gpBg.setCornerRadius(dpToPx(10));
        gpBg.setColor(dark.getAsBoolean() ? 0xFF1E2838 : 0xFFE0F2FE);
        gpBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF2B3A50 : 0xFFBAE6FD);
        btnGroupPicker.setBackground(gpBg);
        btnGroupPicker.setClickable(true);
        btnGroupPicker.setFocusable(true);
        btnGroupPicker.setOnClickListener(v -> {
            sound.accept("tap");
            PopupMenu pm = new PopupMenu(activity, btnGroupPicker);
            for (int i = 0; i < folderList.size(); i++) {
                pm.getMenu().add(0, i, i, "📁 " + folderList.get(i));
            }
            pm.getMenu().add(0, folderList.size(), folderList.size(), "➕ New Group...");
            pm.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id >= 0 && id < folderList.size()) {
                    selectedFolder[0] = folderList.get(id);
                    btnGroupPicker.setText("📁 " + selectedFolder[0]);
                } else if (id == folderList.size()) {
                    showCreateBookmarkGroupDialog(() -> {
                        List<String> up = bookmarkManager.getFolders();
                        if (!up.isEmpty()) {
                            selectedFolder[0] = up.get(up.size() - 1);
                            btnGroupPicker.setText("📁 " + selectedFolder[0]);
                        }
                    });
                }
                return true;
            });
            pm.show();
        });
        root.addView(btnGroupPicker);

        AlertDialog addDialog = builder.setView(root).create();
        if (addDialog.getWindow() != null) {
            addDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Action Buttons Row
        LinearLayout btnRow = new LinearLayout(activity);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, dpToPx(20), 0, 0);

        TextView btnCancel = new TextView(activity);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnCancel.setTextColor(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
        btnCancel.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
        btnCancel.setOnClickListener(v -> addDialog.dismiss());
        btnRow.addView(btnCancel);

        TextView btnSave = new TextView(activity);
        btnSave.setText("Save Bookmark");
        btnSave.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnSave.setTypeface(null, Typeface.BOLD);
        btnSave.setTextColor(0xFF000000);
        btnSave.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));

        GradientDrawable svBg = new GradientDrawable();
        svBg.setCornerRadius(dpToPx(10));
        svBg.setColor(0xFF00E5FF);
        btnSave.setBackground(svBg);
        btnSave.setOnClickListener(v -> {
            sound.accept("tap");
            String t = etTitle.getText().toString().trim();
            String u = etUrl.getText().toString().trim();
            if (!u.isEmpty()) {
                if (!u.startsWith("http://") && !u.startsWith("https://")) u = "https://" + u;
                if (t.isEmpty()) t = u;
                bookmarkManager.addBookmark(t, u, selectedFolder[0], "");
                Toast.makeText(activity, "Bookmark saved to " + selectedFolder[0] + "! ✨", Toast.LENGTH_SHORT).show();
                addDialog.dismiss();
                if (onRefresh != null) onRefresh.run();
            }
        });
        btnRow.addView(btnSave);
        root.addView(btnRow);

        addDialog.show();
    }

    private void showCreateBookmarkGroupDialog(Runnable onRefresh) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dpToPx(24), dpToPx(22), dpToPx(24), dpToPx(18));

        GradientDrawable rootBg = new GradientDrawable();
        rootBg.setCornerRadius(dpToPx(22));
        rootBg.setColor(dark.getAsBoolean() ? 0xFF141926 : 0xFFFFFFFF);
        rootBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF1F293D : 0xFFCBD5E1);
        root.setBackground(rootBg);

        TextView title = new TextView(activity);
        title.setText("New Bookmark Group");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(dark.getAsBoolean() ? 0xFFFFFFFF : 0xFF0F172A);
        root.addView(title);

        TextView lblName = new TextView(activity);
        lblName.setText("GROUP NAME");
        lblName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        lblName.setTypeface(null, Typeface.BOLD);
        lblName.setTextColor(dark.getAsBoolean() ? 0xFF849396 : 0xFF64748B);
        lblName.setPadding(0, dpToPx(14), 0, dpToPx(4));
        root.addView(lblName);

        EditText etName = new EditText(activity);
        etName.setHint("e.g. Research, Work, AI Prompts");
        etName.setTextColor(dark.getAsBoolean() ? 0xFFDFE2F0 : 0xFF0F172A);
        etName.setHintTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF94A3B8);
        etName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        root.addView(etName);

        AlertDialog groupDialog = builder.setView(root).create();
        if (groupDialog.getWindow() != null) {
            groupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout btnRow = new LinearLayout(activity);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, dpToPx(20), 0, 0);

        TextView btnCancel = new TextView(activity);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnCancel.setTextColor(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
        btnCancel.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
        btnCancel.setOnClickListener(v -> groupDialog.dismiss());
        btnRow.addView(btnCancel);

        TextView btnCreate = new TextView(activity);
        btnCreate.setText("Create Group");
        btnCreate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnCreate.setTypeface(null, Typeface.BOLD);
        btnCreate.setTextColor(0xFF000000);
        btnCreate.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));

        GradientDrawable crBg = new GradientDrawable();
        crBg.setCornerRadius(dpToPx(10));
        crBg.setColor(0xFF00E5FF);
        btnCreate.setBackground(crBg);
        btnCreate.setOnClickListener(v -> {
            sound.accept("tap");
            String gName = etName.getText().toString().trim();
            if (!gName.isEmpty()) {
                bookmarkManager.createFolder(gName);
                Toast.makeText(activity, "Group \"" + gName + "\" created! 📁", Toast.LENGTH_SHORT).show();
                groupDialog.dismiss();
                if (onRefresh != null) onRefresh.run();
            }
        });
        btnRow.addView(btnCreate);
        root.addView(btnRow);

        groupDialog.show();
    }

    private void showRenameGroupDialog(String oldName, Runnable onComplete) {
        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        LinearLayout l = new LinearLayout(activity);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dpToPx(24), dpToPx(20), dpToPx(24), dpToPx(16));

        GradientDrawable rBg = new GradientDrawable();
        rBg.setCornerRadius(dpToPx(20));
        rBg.setColor(dark.getAsBoolean() ? 0xFF141926 : 0xFFFFFFFF);
        rBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF1F293D : 0xFFCBD5E1);
        l.setBackground(rBg);

        TextView tv = new TextView(activity);
        tv.setText("Rename Bookmark Group");
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(dark.getAsBoolean() ? 0xFFF1F5F9 : 0xFF0F172A);
        l.addView(tv);

        EditText et = new EditText(activity);
        et.setText(oldName);
        et.setSelection(oldName.length());
        et.setTextColor(dark.getAsBoolean() ? 0xFFDFE2F0 : 0xFF0F172A);
        et.setHintTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF94A3B8);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        l.addView(et);

        AlertDialog renameDialog = b.setView(l).create();
        if (renameDialog.getWindow() != null) {
            renameDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        LinearLayout btnRow = new LinearLayout(activity);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, dpToPx(18), 0, 0);

        TextView btnCancel = new TextView(activity);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnCancel.setTextColor(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
        btnCancel.setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8));
        btnCancel.setOnClickListener(v -> renameDialog.dismiss());
        btnRow.addView(btnCancel);

        TextView btnRename = new TextView(activity);
        btnRename.setText("Rename");
        btnRename.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        btnRename.setTypeface(null, Typeface.BOLD);
        btnRename.setTextColor(0xFF000000);
        btnRename.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));

        GradientDrawable rnBg = new GradientDrawable();
        rnBg.setCornerRadius(dpToPx(10));
        rnBg.setColor(0xFF00E5FF);
        btnRename.setBackground(rnBg);
        btnRename.setOnClickListener(v -> {
            sound.accept("tap");
            String newName = et.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equalsIgnoreCase(oldName)) {
                bookmarkManager.renameFolder(oldName, newName);
                Toast.makeText(activity, "Renamed to \"" + newName + "\"", Toast.LENGTH_SHORT).show();
                renameDialog.dismiss();
                if (onComplete != null) onComplete.run();
            }
        });
        btnRow.addView(btnRename);
        l.addView(btnRow);

        renameDialog.show();
    }

    private void showDeleteGroupDialog(String folderName, Runnable onComplete) {
        new AlertDialog.Builder(activity)
                .setTitle("Delete Group")
                .setMessage("Delete bookmark group \"" + folderName + "\"? Bookmarks inside this group will be moved to Default.")
                .setPositiveButton("Delete", (d, w) -> {
                    bookmarkManager.deleteFolder(folderName);
                    Toast.makeText(activity, "Group \"" + folderName + "\" deleted", Toast.LENGTH_SHORT).show();
                    if (onComplete != null) onComplete.run();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void handleExportBookmarksToFolder(Uri treeUri) {
        if (treeUri == null || bookmarkManager == null) return;
        try {
            androidx.documentfile.provider.DocumentFile pickedDir = androidx.documentfile.provider.DocumentFile.fromTreeUri(activity, treeUri);
            if (pickedDir != null && pickedDir.canWrite()) {
                androidx.documentfile.provider.DocumentFile newFile = pickedDir.createFile("text/html", "caspian_bookmarks_" + System.currentTimeMillis() + ".html");
                if (newFile != null) {
                    try (java.io.OutputStream os = activity.getContentResolver().openOutputStream(newFile.getUri())) {
                        if (os != null) {
                            bookmarkManager.exportNetscapeHtml(os);
                            Toast.makeText(activity, "Bookmarks exported successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(activity, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void handleImportBookmarksFromFile(Uri fileUri) {
        if (fileUri == null || bookmarkManager == null) return;
        try (java.io.InputStream is = activity.getContentResolver().openInputStream(fileUri)) {
            if (is != null) {
                int count = bookmarkManager.importFromHtml(is);
                Toast.makeText(activity, "Imported " + count + " bookmarks!", Toast.LENGTH_SHORT).show();
                if (currentBookmarksRefreshRunnable != null) currentBookmarksRefreshRunnable.run();
            }
        } catch (Exception e) {
            Toast.makeText(activity, "Import failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void showHistoryDialog() {
        showHistoryDialog(0);
    }

    public void showHistoryDialog(int enterDirection) {
        sound.accept("tap");
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_history, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(dark.getAsBoolean() ? 0xFF070A10 : 0xFFF8FAFC));
            dialog.getWindow().setWindowAnimations(0);
        }
        dialogView.setBackgroundColor(dark.getAsBoolean() ? 0xFF070A10 : 0xFFF8FAFC);

        // Single iOS Slide Entrance Transition
        if (motion.getAsBoolean() && enterDirection > 0) {
            dialogView.setTranslationX(dpToPx(80));
            dialogView.setAlpha(0.3f);
            dialogView.animate().translationX(0f).alpha(1f).setDuration(200).setInterpolator(new DecelerateInterpolator(1.6f)).start();
        } else if (motion.getAsBoolean() && enterDirection < 0) {
            dialogView.setTranslationX(-dpToPx(80));
            dialogView.setAlpha(0.3f);
            dialogView.animate().translationX(0f).alpha(1f).setDuration(200).setInterpolator(new DecelerateInterpolator(1.6f)).start();
        }

        View historyRoot = dialogView.findViewById(R.id.history_root);
        View historyHeaderSection = dialogView.findViewById(R.id.history_header_section);
        View historyTabCapsule = dialogView.findViewById(R.id.history_tab_capsule);
        TextView historyTabCapsuleText = dialogView.findViewById(R.id.history_tab_capsule_text);
        TextView badgeCount = dialogView.findViewById(R.id.badge_history_count);
        TextView tabBookmarks = dialogView.findViewById(R.id.tab_header_bookmarks);
        TextView tabSettings = dialogView.findViewById(R.id.tab_header_settings);
        View historySearchCapsule = dialogView.findViewById(R.id.history_search_capsule);
        ImageView historySearchIcon = dialogView.findViewById(R.id.history_search_icon);
        EditText searchInput = dialogView.findViewById(R.id.history_search_input);
        View historySearchDivider = dialogView.findViewById(R.id.history_search_divider);
        TextView btnSelect = dialogView.findViewById(R.id.btn_history_select);
        ImageView btnOptions = dialogView.findViewById(R.id.btn_history_options);
        ImageView closeBtn = dialogView.findViewById(R.id.history_close_btn);

        if (historyRoot != null) {
            historyRoot.setBackgroundColor(dark.getAsBoolean() ? 0xFF070A10 : 0xFFF8FAFC);
        }
        if (historyHeaderSection != null) {
            GradientDrawable hSecBg = new GradientDrawable();
            hSecBg.setColor(dark.getAsBoolean() ? 0xFF070A10 : 0xFFFFFFFF);
            hSecBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x1FFFFFFF : 0x120F172A);
            historyHeaderSection.setBackground(hSecBg);
        }
        if (historyTabCapsule != null) {
            GradientDrawable capBg = new GradientDrawable();
            capBg.setCornerRadius(dpToPx(16));
            capBg.setColor(dark.getAsBoolean() ? 0xD0121826 : 0xE6FFFFFF);
            capBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);
            historyTabCapsule.setBackground(capBg);
        }
        if (historyTabCapsuleText != null) {
            historyTabCapsuleText.setTextColor(dark.getAsBoolean() ? 0xFFFFFFFF : 0xFF0284C7);
        }
        if (badgeCount != null) {
            GradientDrawable bcBg = new GradientDrawable();
            bcBg.setCornerRadius(dpToPx(10));
            bcBg.setColor(dark.getAsBoolean() ? 0x3322D3EE : 0x180284C7);
            bcBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x5522D3EE : 0x300284C7);
            badgeCount.setBackground(bcBg);
            badgeCount.setTextColor(dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);
        }
        if (tabBookmarks != null) {
            tabBookmarks.setTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF475569);
        }
        if (tabSettings != null) {
            tabSettings.setTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF475569);
        }

        if (historySearchCapsule != null) {
            GradientDrawable sBg = new GradientDrawable();
            sBg.setCornerRadius(dpToPx(16));
            sBg.setColor(dark.getAsBoolean() ? 0xD0121826 : 0xF0FFFFFF);
            sBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x22FFFFFF : 0x140F172A);
            historySearchCapsule.setBackground(sBg);
        }
        if (historySearchIcon != null) {
            historySearchIcon.setColorFilter(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
        }
        if (historySearchDivider != null) {
            historySearchDivider.setBackgroundColor(dark.getAsBoolean() ? 0x24FFFFFF : 0x140F172A);
        }
        if (searchInput != null) {
            searchInput.setTextColor(dark.getAsBoolean() ? 0xFFF8FAFC : 0xFF0F172A);
            searchInput.setHintTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF94A3B8);
        }
        if (btnSelect != null) {
            GradientDrawable selBg = new GradientDrawable();
            selBg.setCornerRadius(dpToPx(8));
            selBg.setColor(dark.getAsBoolean() ? 0x2822D3EE : 0x180284C7);
            selBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x4422D3EE : 0x300284C7);
            btnSelect.setBackground(selBg);
            btnSelect.setTextColor(dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);
        }
        if (btnOptions != null) {
            GradientDrawable optBg = new GradientDrawable();
            optBg.setCornerRadius(dpToPx(8));
            optBg.setColor(dark.getAsBoolean() ? 0x1CFFFFFF : 0x0C0F172A);
            optBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x24FFFFFF : 0x140F172A);
            btnOptions.setBackground(optBg);
            btnOptions.setColorFilter(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF475569);
        }
        if (closeBtn != null) {
            GradientDrawable clsBg = new GradientDrawable();
            clsBg.setCornerRadius(dpToPx(8));
            clsBg.setColor(dark.getAsBoolean() ? 0x1CFFFFFF : 0x0C0F172A);
            clsBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x24FFFFFF : 0x140F172A);
            closeBtn.setBackground(clsBg);
            closeBtn.setColorFilter(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF475569);
        }

        TextView chipAll = dialogView.findViewById(R.id.chip_filter_all);
        TextView chip1h = dialogView.findViewById(R.id.chip_filter_1h);
        TextView chip24h = dialogView.findViewById(R.id.chip_filter_24h);
        TextView chip1w = dialogView.findViewById(R.id.chip_filter_1w);

        LinearLayout listContainer = dialogView.findViewById(R.id.history_list_container);
        TextView emptyView = dialogView.findViewById(R.id.history_empty_view);

        View historyBottomToolbar = dialogView.findViewById(R.id.history_bottom_toolbar);
        if (historyBottomToolbar != null) {
            historyBottomToolbar.setBackgroundColor(dark.getAsBoolean() ? 0xFF070A10 : 0xFFF8FAFC);
        }
        View historyBottomBarCard = dialogView.findViewById(R.id.history_bottom_bar_card);
        View bottomNormalBar = dialogView.findViewById(R.id.bottom_normal_bar);
        View bottomSelectBar = dialogView.findViewById(R.id.bottom_select_bar);
        View timeRangeSelector = dialogView.findViewById(R.id.layout_time_range_selector);
        View pillTimeRangeDropdown = dialogView.findViewById(R.id.pill_time_range_dropdown);
        ImageView iconTimeRangeClock = dialogView.findViewById(R.id.icon_time_range_clock);
        TextView labelTimeRange = dialogView.findViewById(R.id.label_time_range);
        ImageView iconTimeRangeChevron = dialogView.findViewById(R.id.icon_time_range_chevron);
        TextView textSelectedTimeRange = dialogView.findViewById(R.id.text_selected_time_range);

        View btnClearCookies = dialogView.findViewById(R.id.btn_clear_cookies);
        ImageView iconClearCookies = dialogView.findViewById(R.id.icon_clear_cookies);
        TextView textClearCookies = dialogView.findViewById(R.id.text_clear_cookies);

        View btnClearData = dialogView.findViewById(R.id.btn_clear_data);
        ImageView iconClearData = dialogView.findViewById(R.id.icon_clear_data);
        TextView textClearData = dialogView.findViewById(R.id.text_clear_data);

        View btnClearBoth = dialogView.findViewById(R.id.btn_clear_both);
        ImageView iconClearBoth = dialogView.findViewById(R.id.icon_clear_both);
        TextView textClearBoth = dialogView.findViewById(R.id.text_clear_both);

        if (historyBottomBarCard != null) {
            GradientDrawable bcBg = new GradientDrawable();
            bcBg.setCornerRadius(dpToPx(18));
            bcBg.setColor(dark.getAsBoolean() ? 0xEE121826 : 0xF5FFFFFF);
            bcBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x33FFFFFF : 0x1E0F172A);
            historyBottomBarCard.setBackground(bcBg);
        }
        if (bottomNormalBar != null) {
            bottomNormalBar.setBackground(null);
        }
        if (bottomSelectBar != null) {
            bottomSelectBar.setBackground(null);
        }
        if (pillTimeRangeDropdown != null) {
            GradientDrawable pillBg = new GradientDrawable();
            pillBg.setCornerRadius(dpToPx(12));
            pillBg.setColor(dark.getAsBoolean() ? 0x20FFFFFF : 0x0C0F172A);
            pillBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x26FFFFFF : 0x140F172A);
            pillTimeRangeDropdown.setBackground(pillBg);
        }
        if (iconTimeRangeClock != null) iconTimeRangeClock.setColorFilter(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
        if (labelTimeRange != null) labelTimeRange.setTextColor(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
        if (iconTimeRangeChevron != null) iconTimeRangeChevron.setColorFilter(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
        if (textSelectedTimeRange != null) textSelectedTimeRange.setTextColor(dark.getAsBoolean() ? 0xFFF8FAFC : 0xFF0F172A);

        if (btnClearCookies != null) {
            GradientDrawable cBg = new GradientDrawable();
            cBg.setCornerRadius(dpToPx(10));
            cBg.setColor(dark.getAsBoolean() ? 0x20FFFFFF : 0x0C0F172A);
            cBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x26FFFFFF : 0x140F172A);
            btnClearCookies.setBackground(cBg);
        }
        if (iconClearCookies != null) iconClearCookies.setColorFilter(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF0284C7);
        if (textClearCookies != null) textClearCookies.setTextColor(dark.getAsBoolean() ? 0xFFF8FAFC : 0xFF0F172A);

        if (btnClearData != null) {
            GradientDrawable dBg = new GradientDrawable();
            dBg.setCornerRadius(dpToPx(10));
            dBg.setColor(dark.getAsBoolean() ? 0x2822D3EE : 0x180284C7);
            dBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x4422D3EE : 0x300284C7);
            btnClearData.setBackground(dBg);
        }
        if (iconClearData != null) iconClearData.setColorFilter(dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);
        if (textClearData != null) textClearData.setTextColor(dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);

        if (btnClearBoth != null) {
            GradientDrawable bBg = new GradientDrawable();
            bBg.setCornerRadius(dpToPx(10));
            bBg.setColor(dark.getAsBoolean() ? 0x261217 : 0xFFFEF2F2);
            bBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF4D1720 : 0xFFFECACA);
            btnClearBoth.setBackground(bBg);
        }
        if (iconClearBoth != null) iconClearBoth.setColorFilter(dark.getAsBoolean() ? 0xFFEF4444 : 0xFFEF4444);
        if (textClearBoth != null) textClearBoth.setTextColor(dark.getAsBoolean() ? 0xFFFCA5A5 : 0xFFDC2626);

        TextView textSelectedCount = dialogView.findViewById(R.id.text_selected_count);
        TextView btnSelectAllToggle = dialogView.findViewById(R.id.btn_select_all_toggle);
        TextView btnDeleteSelected = dialogView.findViewById(R.id.btn_delete_selected);
        TextView btnCancelSelect = dialogView.findViewById(R.id.btn_cancel_select);

        final boolean[] isSelectMode = new boolean[]{false};
        final Set<Long> selectedIds = new HashSet<>();
        final String[] activeFilter = new String[]{"all"};
        final String[] selectedTimeRange = new String[]{"24h"};
        final List<HistoryManager.HistoryEntry> currentVisibleEntries = new ArrayList<>();

        java.text.SimpleDateFormat timeSdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        java.text.SimpleDateFormat dayFormat = new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault());

        Runnable updateChipsVisuals = () -> {
            TextView[] chips = new TextView[]{chipAll, chip1h, chip24h, chip1w};
            String[] filterVals = new String[]{"all", "1h", "24h", "1w"};
            for (int ci = 0; ci < chips.length; ci++) {
                TextView cp = chips[ci];
                if (cp == null) continue;
                boolean act = filterVals[ci].equals(activeFilter[0]);
                GradientDrawable cBg = new GradientDrawable();
                cBg.setCornerRadius(dpToPx(14));
                if (act) {
                    cBg.setColor(dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);
                    cBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF22D3EE : 0xFF0284C7);
                    cp.setTextColor(dark.getAsBoolean() ? 0xFF070A10 : 0xFFFFFFFF);
                } else {
                    cBg.setColor(dark.getAsBoolean() ? 0x1CFFFFFF : 0x0C0F172A);
                    cBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x24FFFFFF : 0x140F172A);
                    cp.setTextColor(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
                }
                cp.setBackground(cBg);
            }
        };
        updateChipsVisuals.run();

        final Runnable[] refreshList = new Runnable[1];
        refreshList[0] = () -> {
            listContainer.removeAllViews();
            currentVisibleEntries.clear();
            String query = searchInput != null ? searchInput.getText().toString() : "";
            List<HistoryManager.HistoryEntry> rawEntries = HistoryManager.getInstance(activity).getHistory(query);

            int totalCount = HistoryManager.getInstance(activity).getTotalHistoryCount();
            if (badgeCount != null) badgeCount.setText(String.valueOf(totalCount));

            long now = System.currentTimeMillis();
            for (HistoryManager.HistoryEntry e : rawEntries) {
                if ("1h".equals(activeFilter[0])) {
                    if (e.timestamp < now - (3600L * 1000L)) continue;
                } else if ("24h".equals(activeFilter[0])) {
                    if (e.timestamp < now - (24L * 3600L * 1000L)) continue;
                } else if ("1w".equals(activeFilter[0])) {
                    if (e.timestamp < now - (7L * 24L * 3600L * 1000L)) continue;
                }
                currentVisibleEntries.add(e);
            }

            if (currentVisibleEntries.isEmpty()) {
                if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
            } else {
                if (emptyView != null) emptyView.setVisibility(View.GONE);

                java.util.Calendar calNow = java.util.Calendar.getInstance();
                int curYear = calNow.get(java.util.Calendar.YEAR);
                int curDay = calNow.get(java.util.Calendar.DAY_OF_YEAR);

                // Group entries by date
                java.util.LinkedHashMap<String, List<HistoryManager.HistoryEntry>> groups = new java.util.LinkedHashMap<>();
                for (HistoryManager.HistoryEntry entry : currentVisibleEntries) {
                    java.util.Calendar calEntry = java.util.Calendar.getInstance();
                    calEntry.setTimeInMillis(entry.timestamp);
                    int entryYear = calEntry.get(java.util.Calendar.YEAR);
                    int entryDay = calEntry.get(java.util.Calendar.DAY_OF_YEAR);

                    String groupKey;
                    if (curYear == entryYear && curDay == entryDay) {
                        groupKey = "TODAY • " + dayFormat.format(new Date(entry.timestamp)).toUpperCase(Locale.ROOT);
                    } else if (curYear == entryYear && curDay - 1 == entryDay) {
                        groupKey = "YESTERDAY • " + dayFormat.format(new Date(entry.timestamp)).toUpperCase(Locale.ROOT);
                    } else {
                        groupKey = dayFormat.format(new Date(entry.timestamp)).toUpperCase(Locale.ROOT);
                    }

                    if (!groups.containsKey(groupKey)) {
                        groups.put(groupKey, new ArrayList<>());
                    }
                    groups.get(groupKey).add(entry);
                }

                for (Map.Entry<String, List<HistoryManager.HistoryEntry>> group : groups.entrySet()) {
                    String dateKey = group.getKey();
                    List<HistoryManager.HistoryEntry> dayEntries = group.getValue();

                    // Section Header
                    LinearLayout secHeader = new LinearLayout(activity);
                    secHeader.setOrientation(LinearLayout.HORIZONTAL);
                    secHeader.setGravity(Gravity.CENTER_VERTICAL);
                    secHeader.setPadding(dpToPx(2), dpToPx(8), dpToPx(2), dpToPx(4));

                    TextView secTitle = new TextView(activity);
                    secTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    secTitle.setText(dateKey);
                    secTitle.setTextColor(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF475569);
                    secTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                    secTitle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
                    secHeader.addView(secTitle);

                    TextView secDelete = new TextView(activity);
                    secDelete.setText(dateKey.startsWith("TODAY") ? "Delete Today" : (dateKey.startsWith("YESTERDAY") ? "Delete Yesterday" : "Delete Day"));
                    secDelete.setTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF94A3B8);
                    secDelete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
                    secDelete.setClickable(true);
                    secDelete.setFocusable(true);
                    secDelete.setOnClickListener(v -> {
                        sound.accept("tap");
                        List<Long> idsToDelete = new ArrayList<>();
                        for (HistoryManager.HistoryEntry e : dayEntries) idsToDelete.add(e.id);
                        HistoryManager.getInstance(activity).deleteEntries(idsToDelete);
                        refreshList[0].run();
                        Toast.makeText(activity, "Cleared " + dateKey, Toast.LENGTH_SHORT).show();
                    });
                    secHeader.addView(secDelete);
                    listContainer.addView(secHeader);

                    // Group Items
                    for (HistoryManager.HistoryEntry entry : dayEntries) {
                        LinearLayout card = new LinearLayout(activity);
                        card.setOrientation(LinearLayout.HORIZONTAL);
                        card.setGravity(Gravity.CENTER_VERTICAL);
                        card.setPadding(dpToPx(12), dpToPx(10), dpToPx(10), dpToPx(10));

                        GradientDrawable cardBg = new GradientDrawable();
                        cardBg.setCornerRadius(dpToPx(18));
                        cardBg.setColor(dark.getAsBoolean() ? 0xD0121826 : 0xF2FFFFFF);
                        cardBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x22FFFFFF : 0x120F172A);
                        card.setBackground(cardBg);

                        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        cardLp.bottomMargin = dpToPx(7);
                        card.setLayoutParams(cardLp);

                        // Checkbox for Select Mode
                        if (isSelectMode[0]) {
                            TextView checkIcon = new TextView(activity);
                            boolean isChecked = selectedIds.contains(entry.id);
                            checkIcon.setText(isChecked ? "✓" : "○");
                            checkIcon.setTextColor(isChecked ? 0xFF22D3EE : 0xFF64748B);
                            checkIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
                            checkIcon.setTypeface(null, Typeface.BOLD);
                            checkIcon.setPadding(0, 0, dpToPx(10), 0);
                            card.addView(checkIcon);
                        }

                        // Squircle Icon Container (32dp x 32dp)
                        FrameLayout iconBox = new FrameLayout(activity);
                        LinearLayout.LayoutParams ibLp = new LinearLayout.LayoutParams(dpToPx(32), dpToPx(32));
                        ibLp.setMarginEnd(dpToPx(10));
                        iconBox.setLayoutParams(ibLp);

                        GradientDrawable ibGd = new GradientDrawable();
                        ibGd.setColor(dark.getAsBoolean() ? 0x1AFFFFFF : 0x0A0F172A);
                        ibGd.setCornerRadius(dpToPx(10));
                        ibGd.setStroke(dpToPx(1), dark.getAsBoolean() ? 0x24FFFFFF : 0x100F172A);
                        iconBox.setBackground(ibGd);

                        ImageView iv = new ImageView(activity);
                        FrameLayout.LayoutParams ivLp = new FrameLayout.LayoutParams(dpToPx(16), dpToPx(16));
                        ivLp.gravity = Gravity.CENTER;
                        iv.setLayoutParams(ivLp);
                        if (entry.url.contains("google.")) {
                            iv.setImageResource(R.drawable.ic_stitch_google);
                        } else {
                            iv.setImageResource(R.drawable.ic_stitch_globe);
                            iv.setColorFilter(0xFF38BDF8);
                        }
                        iconBox.addView(iv);
                        card.addView(iconBox);

                        // Text Details Column
                        LinearLayout textCol = new LinearLayout(activity);
                        textCol.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        textCol.setLayoutParams(tcLp);

                        TextView titleTv = new TextView(activity);
                        titleTv.setText(entry.title != null && !entry.title.isEmpty() ? entry.title : entry.url);
                        titleTv.setTextColor(dark.getAsBoolean() ? 0xFFF8FAFC : 0xFF0F172A);
                        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
                        titleTv.setTypeface(null, Typeface.BOLD);
                        titleTv.setSingleLine(true);
                        titleTv.setEllipsize(android.text.TextUtils.TruncateAt.END);

                        TextView urlTv = new TextView(activity);
                        urlTv.setText(displayUrl.apply(entry.url));
                        urlTv.setTextColor(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
                        urlTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
                        urlTv.setSingleLine(true);
                        urlTv.setEllipsize(android.text.TextUtils.TruncateAt.END);

                        textCol.addView(titleTv);
                        textCol.addView(urlTv);
                        card.addView(textCol);

                        // Right Column: Time + 3-dots Menu
                        LinearLayout rightCol = new LinearLayout(activity);
                        rightCol.setOrientation(LinearLayout.HORIZONTAL);
                        rightCol.setGravity(Gravity.CENTER_VERTICAL);
                        rightCol.setPadding(dpToPx(6), 0, 0, 0);

                        TextView timeTv = new TextView(activity);
                        timeTv.setText(timeSdf.format(new Date(entry.timestamp)));
                        timeTv.setTextColor(dark.getAsBoolean() ? 0xFF64748B : 0xFF94A3B8);
                        timeTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
                        timeTv.setTypeface(Typeface.MONOSPACE);
                        rightCol.addView(timeTv);

                        ImageView moreBtn = new ImageView(activity);
                        LinearLayout.LayoutParams mbLp = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
                        mbLp.setMarginStart(dpToPx(4));
                        moreBtn.setLayoutParams(mbLp);
                        moreBtn.setImageResource(R.drawable.ic_stitch_more_vert);
                        moreBtn.setColorFilter(dark.getAsBoolean() ? 0xFF94A3B8 : 0xFF64748B);
                        GradientDrawable mbBg = new GradientDrawable();
                        mbBg.setShape(GradientDrawable.OVAL);
                        mbBg.setColor(dark.getAsBoolean() ? 0xFF161B22 : 0xFFF1F5F9);
                        mbBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF21262D : 0xFFE2E8F0);
                        moreBtn.setBackground(mbBg);
                        moreBtn.setClickable(true);
                        moreBtn.setFocusable(true);
                        moreBtn.setOnClickListener(v -> {
                            sound.accept("tap");
                            PopupMenu popup = new PopupMenu(activity, moreBtn);
                            popup.getMenu().add("Open in New Tab");
                            popup.getMenu().add("Copy URL");
                            popup.getMenu().add("Delete Entry");
                            popup.setOnMenuItemClickListener(item -> {
                                if ("Open in New Tab".equals(item.getTitle())) {
                                    dialog.dismiss();
                                    activity.addNewTab("web", null, entry.url, false);
                                } else if ("Copy URL".equals(item.getTitle())) {
                                    android.content.ClipboardManager cm = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                                    cm.setPrimaryClip(android.content.ClipData.newPlainText("URL", entry.url));
                                    Toast.makeText(activity, "Copied URL to clipboard", Toast.LENGTH_SHORT).show();
                                } else if ("Delete Entry".equals(item.getTitle())) {
                                    HistoryManager.getInstance(activity).deleteEntry(entry.id);
                                    refreshList[0].run();
                                }
                                return true;
                            });
                            popup.show();
                        });
                        rightCol.addView(moreBtn);
                        card.addView(rightCol);

                        card.setOnClickListener(v -> {
                            sound.accept("tap");
                            if (isSelectMode[0]) {
                                if (selectedIds.contains(entry.id)) {
                                    selectedIds.remove(entry.id);
                                } else {
                                    selectedIds.add(entry.id);
                                }
                                if (textSelectedCount != null) {
                                    textSelectedCount.setText(selectedIds.size() + " selected");
                                }
                                refreshList[0].run();
                            } else {
                                dialog.dismiss();
                                TabItem active = activeTab.get();
                                if (active != null) {
                                    active.webView.loadUrl(entry.url);
                                } else {
                                    activity.addNewTab("web", null, entry.url, false);
                                }
                            }
                        });

                        listContainer.addView(card);
                    }
                }
            }
        };

        refreshList[0].run();

        // Search Input Listener
        if (searchInput != null) {
            searchInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refreshList[0].run(); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Close Button
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> dialog.dismiss());
        }

        // Header Bookmarks & Settings Navigation
        if (tabBookmarks != null) {
            tabBookmarks.setOnClickListener(v -> {
                sound.accept("tap");
                dialog.dismiss();
                showBookmarksDialog(-1);
            });
        }
        if (tabSettings != null) {
            tabSettings.setOnClickListener(v -> {
                sound.accept("tap");
                dialog.dismiss();
                activity.openBrowserSettings();
            });
        }

        // Filter Chips Click Listeners
        View.OnClickListener chipListener = v -> {
            sound.accept("tap");
            if (v == chipAll) {
                activeFilter[0] = "all";
                selectedTimeRange[0] = "all";
                if (textSelectedTimeRange != null) textSelectedTimeRange.setText("All Entries");
            } else if (v == chip1h) {
                activeFilter[0] = "1h";
                selectedTimeRange[0] = "1h";
                if (textSelectedTimeRange != null) textSelectedTimeRange.setText("Last 1 Hour");
            } else if (v == chip24h) {
                activeFilter[0] = "24h";
                selectedTimeRange[0] = "24h";
                if (textSelectedTimeRange != null) textSelectedTimeRange.setText("Last 24 Hours");
            } else if (v == chip1w) {
                activeFilter[0] = "1w";
                selectedTimeRange[0] = "1w";
                if (textSelectedTimeRange != null) textSelectedTimeRange.setText("Last 1 Week");
            }
            updateChipsVisuals.run();
            refreshList[0].run();
        };
        if (chipAll != null) chipAll.setOnClickListener(chipListener);
        if (chip1h != null) chip1h.setOnClickListener(chipListener);
        if (chip24h != null) chip24h.setOnClickListener(chipListener);
        if (chip1w != null) chip1w.setOnClickListener(chipListener);

        // Select Mode Toggle
        if (btnSelect != null) {
            btnSelect.setOnClickListener(v -> {
                sound.accept("tap");
                isSelectMode[0] = !isSelectMode[0];
                if (isSelectMode[0]) {
                    btnSelect.setText("Done");
                    if (bottomNormalBar != null) bottomNormalBar.setVisibility(View.GONE);
                    if (bottomSelectBar != null) bottomSelectBar.setVisibility(View.VISIBLE);
                } else {
                    selectedIds.clear();
                    btnSelect.setText("Select");
                    if (bottomNormalBar != null) bottomNormalBar.setVisibility(View.VISIBLE);
                    if (bottomSelectBar != null) bottomSelectBar.setVisibility(View.GONE);
                }
                refreshList[0].run();
            });
        }

        // Options 3-dots Menu
        if (btnOptions != null) {
            btnOptions.setOnClickListener(v -> {
                sound.accept("tap");
                PopupMenu popup = new PopupMenu(activity, btnOptions);
                popup.getMenu().add("Select Multiple");
                popup.getMenu().add("Select All");
                popup.getMenu().add("Clear Entire History");
                popup.setOnMenuItemClickListener(item -> {
                    if ("Select Multiple".equals(item.getTitle())) {
                        isSelectMode[0] = true;
                        if (btnSelect != null) btnSelect.setText("Done");
                        if (bottomNormalBar != null) bottomNormalBar.setVisibility(View.GONE);
                        if (bottomSelectBar != null) bottomSelectBar.setVisibility(View.VISIBLE);
                        refreshList[0].run();
                    } else if ("Select All".equals(item.getTitle())) {
                        isSelectMode[0] = true;
                        if (btnSelect != null) btnSelect.setText("Done");
                        if (bottomNormalBar != null) bottomNormalBar.setVisibility(View.GONE);
                        if (bottomSelectBar != null) bottomSelectBar.setVisibility(View.VISIBLE);
                        selectedIds.clear();
                        for (HistoryManager.HistoryEntry e : currentVisibleEntries) selectedIds.add(e.id);
                        if (textSelectedCount != null) textSelectedCount.setText(selectedIds.size() + " selected");
                        if (btnSelectAllToggle != null) btnSelectAllToggle.setText("Deselect All");
                        refreshList[0].run();
                    } else if ("Clear Entire History".equals(item.getTitle())) {
                        new AlertDialog.Builder(activity)
                                .setTitle("Clear History")
                                .setMessage("Are you sure you want to delete all browsing history?")
                                .setPositiveButton("Clear All", (d, w) -> {
                                    HistoryManager.getInstance(activity).clearAllHistory();
                                    refreshList[0].run();
                                    Toast.makeText(activity, "🗑️ All browsing history cleared", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                    return true;
                });
                popup.show();
            });
        }

        // Bottom Time Range Dropdown Selector (Custom Stitch Obsidian UI)
        View.OnClickListener timeRangePicker = v -> {
            sound.accept("tap");
            com.google.android.material.bottomsheet.BottomSheetDialog pickerDialog =
                    new com.google.android.material.bottomsheet.BottomSheetDialog(activity);
            View pickerView = LayoutInflater.from(activity).inflate(R.layout.dialog_stitch_time_range_picker, null);
            GradientDrawable pickerBg = new GradientDrawable();
            pickerBg.setCornerRadii(new float[]{dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20), 0, 0, 0, 0});
            pickerBg.setColor(dark.getAsBoolean() ? 0xFF0A0E17 : 0xFFFFFFFF);
            pickerBg.setStroke(dpToPx(1), dark.getAsBoolean() ? 0xFF1E2433 : 0xFFE2E8F0);
            pickerView.setBackground(pickerBg);

            if (!dark.getAsBoolean()) {
                int[] optIds = new int[]{R.id.range_opt_1h, R.id.range_opt_24h, R.id.range_opt_1w, R.id.range_opt_all};
                for (int optId : optIds) {
                    View row = pickerView.findViewById(optId);
                    if (row instanceof ViewGroup) {
                        ViewGroup vg = (ViewGroup) row;
                        for (int c = 0; c < vg.getChildCount(); c++) {
                            View child = vg.getChildAt(c);
                            if (child instanceof ViewGroup) {
                                ViewGroup childVg = (ViewGroup) child;
                                for (int gc = 0; gc < childVg.getChildCount(); gc++) {
                                    View gChild = childVg.getChildAt(gc);
                                    if (gChild instanceof TextView) {
                                        TextView tv = (TextView) gChild;
                                        if (tv.getTextSize() > dpToPx(12)) {
                                            tv.setTextColor(0xFF0F172A);
                                        } else {
                                            tv.setTextColor(0xFF64748B);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            pickerDialog.setContentView(pickerView);
            if (pickerDialog.getWindow() != null) {
                View bs = pickerDialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bs != null) bs.setBackgroundResource(android.R.color.transparent);
            }

            View check1h = pickerView.findViewById(R.id.check_range_1h);
            View check24h = pickerView.findViewById(R.id.check_range_24h);
            View check1w = pickerView.findViewById(R.id.check_range_1w);
            View checkAll = pickerView.findViewById(R.id.check_range_all);

            String cur = activeFilter[0];
            if (check1h != null) check1h.setVisibility("1h".equals(cur) ? View.VISIBLE : View.GONE);
            if (check24h != null) check24h.setVisibility("24h".equals(cur) ? View.VISIBLE : View.GONE);
            if (check1w != null) check1w.setVisibility("1w".equals(cur) ? View.VISIBLE : View.GONE);
            if (checkAll != null) checkAll.setVisibility("all".equals(cur) ? View.VISIBLE : View.GONE);

            View opt1h = pickerView.findViewById(R.id.range_opt_1h);
            View opt24h = pickerView.findViewById(R.id.range_opt_24h);
            View opt1w = pickerView.findViewById(R.id.range_opt_1w);
            View optAll = pickerView.findViewById(R.id.range_opt_all);

            View.OnClickListener optClick = optV -> {
                sound.accept("tap");
                if (optV == opt1h) {
                    activeFilter[0] = "1h";
                    selectedTimeRange[0] = "1h";
                    if (textSelectedTimeRange != null) textSelectedTimeRange.setText("Last 1 Hour");
                } else if (optV == opt24h) {
                    activeFilter[0] = "24h";
                    selectedTimeRange[0] = "24h";
                    if (textSelectedTimeRange != null) textSelectedTimeRange.setText("Last 24 Hours");
                } else if (optV == opt1w) {
                    activeFilter[0] = "1w";
                    selectedTimeRange[0] = "1w";
                    if (textSelectedTimeRange != null) textSelectedTimeRange.setText("Last 1 Week");
                } else {
                    activeFilter[0] = "all";
                    selectedTimeRange[0] = "all";
                    if (textSelectedTimeRange != null) textSelectedTimeRange.setText("All Entries");
                }
                updateChipsVisuals.run();
                refreshList[0].run();
                pickerDialog.dismiss();
            };

            if (opt1h != null) opt1h.setOnClickListener(optClick);
            if (opt24h != null) opt24h.setOnClickListener(optClick);
            if (opt1w != null) opt1w.setOnClickListener(optClick);
            if (optAll != null) optAll.setOnClickListener(optClick);

            pickerDialog.show();
        };
        if (timeRangeSelector != null) timeRangeSelector.setOnClickListener(timeRangePicker);

        // Clear Buttons
        if (btnClearCookies != null) {
            btnClearCookies.setOnClickListener(v -> {
                sound.accept("tap");
                HistoryManager.clearCookies();
                Toast.makeText(activity, "🍪 Cookies cleared", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnClearData != null) {
            btnClearData.setOnClickListener(v -> {
                sound.accept("tap");
                HistoryManager.clearWebData();
                Toast.makeText(activity, "🧹 Browsing cache & data cleared", Toast.LENGTH_SHORT).show();
            });
        }
        if (btnClearBoth != null) {
            btnClearBoth.setOnClickListener(v -> {
                sound.accept("tap");
                long cutoff = 0;
                if ("1h".equals(selectedTimeRange[0])) cutoff = System.currentTimeMillis() - 3600_000L;
                else if ("24h".equals(selectedTimeRange[0])) cutoff = System.currentTimeMillis() - 86400_000L;
                else if ("1w".equals(selectedTimeRange[0])) cutoff = System.currentTimeMillis() - 7 * 86400_000L;

                if (cutoff > 0) {
                    HistoryManager.getInstance(activity).clearHistorySince(cutoff);
                } else {
                    HistoryManager.getInstance(activity).clearAllHistory();
                }
                HistoryManager.clearCookiesAndCache();
                refreshList[0].run();
                Toast.makeText(activity, "✨ History, cookies & cache cleared", Toast.LENGTH_SHORT).show();
            });
        }

        // Selection Action Bar Controls
        if (btnSelectAllToggle != null) {
            btnSelectAllToggle.setOnClickListener(v -> {
                sound.accept("tap");
                if (selectedIds.size() >= currentVisibleEntries.size() && !currentVisibleEntries.isEmpty()) {
                    selectedIds.clear();
                    btnSelectAllToggle.setText("Select All");
                } else {
                    for (HistoryManager.HistoryEntry e : currentVisibleEntries) {
                        selectedIds.add(e.id);
                    }
                    btnSelectAllToggle.setText("Deselect All");
                }
                if (textSelectedCount != null) textSelectedCount.setText(selectedIds.size() + " selected");
                refreshList[0].run();
            });
        }

        if (btnDeleteSelected != null) {
            btnDeleteSelected.setOnClickListener(v -> {
                sound.accept("tap");
                if (selectedIds.isEmpty()) return;
                int count = selectedIds.size();
                HistoryManager.getInstance(activity).deleteEntries(selectedIds);
                selectedIds.clear();
                isSelectMode[0] = false;
                if (btnSelect != null) btnSelect.setText("Select");
                if (bottomNormalBar != null) bottomNormalBar.setVisibility(View.VISIBLE);
                if (bottomSelectBar != null) bottomSelectBar.setVisibility(View.GONE);
                refreshList[0].run();
                Toast.makeText(activity, "🗑️ Deleted " + count + " items", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnCancelSelect != null) {
            btnCancelSelect.setOnClickListener(v -> {
                sound.accept("tap");
                selectedIds.clear();
                isSelectMode[0] = false;
                if (btnSelect != null) btnSelect.setText("Select");
                if (bottomNormalBar != null) bottomNormalBar.setVisibility(View.VISIBLE);
                if (bottomSelectBar != null) bottomSelectBar.setVisibility(View.GONE);
                refreshList[0].run();
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

}
