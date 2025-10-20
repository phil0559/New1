package com.example.new1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Décoration chargée de dessiner une bordure englobant chaque contenant non vide et ses descendants visibles.
 */
class RoomContentRankDecoration extends RecyclerView.ItemDecoration {

    private final RoomContentAdapter adapter;
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF childBounds = new RectF();
    private final RectF groupBounds = new RectF();
    private final int[] parentLocationOnScreen = new int[2];
    private final int[] childLocationOnScreen = new int[2];
    private final float cornerRadius;
    private final float halfStrokeWidth;
    private final int[] borderColors;

    RoomContentRankDecoration(@NonNull Context context, @NonNull RoomContentAdapter adapter) {
        this.adapter = adapter;
        borderPaint.setStyle(Paint.Style.STROKE);
        float strokeWidth = Math.max(1f,
                context.getResources().getDimension(R.dimen.room_content_card_border_width_filled));
        borderPaint.setStrokeWidth(strokeWidth);
        cornerRadius = context.getResources().getDimension(R.dimen.room_content_card_corner_radius);
        halfStrokeWidth = strokeWidth / 2f;
        borderColors = new int[] {
                ContextCompat.getColor(context, R.color.room_content_hierarchy_level_0_border),
                ContextCompat.getColor(context, R.color.room_content_hierarchy_level_1_border),
                ContextCompat.getColor(context, R.color.room_content_hierarchy_level_2_border),
                ContextCompat.getColor(context, R.color.room_content_hierarchy_level_3_border)
        };
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent,
            @NonNull RecyclerView.State state) {
        RecyclerView.Adapter<?> currentAdapter = parent.getAdapter();
        if (currentAdapter != adapter) {
            return;
        }
        int childCount = parent.getChildCount();
        if (childCount == 0) {
            return;
        }
        for (int index = 0; index < childCount; index++) {
            View child = parent.getChildAt(index);
            if (child == null || child.getVisibility() != View.VISIBLE || child.getHeight() <= 0) {
                continue;
            }
            int adapterPosition = parent.getChildAdapterPosition(child);
            if (adapterPosition == RecyclerView.NO_POSITION) {
                continue;
            }
            RoomContentItem item = adapter.getItemAt(adapterPosition);
            if (item == null || !item.isContainer() || !item.hasAttachedItems()) {
                continue;
            }
            View background = child.findViewById(R.id.container_room_content_root);
            View target = background != null ? background : child;
            if (target.getWidth() <= 0 || target.getHeight() <= 0) {
                continue;
            }
            computeBoundsRelativeToRecyclerView(parent, target, childBounds);
            float groupLeft = childBounds.left;
            float groupTop = childBounds.top;
            float groupRight = childBounds.right;
            float groupBottom = childBounds.bottom;
            String rankLabel = adapter.getRankLabelForPosition(adapterPosition);
            if (rankLabel.isEmpty()) {
                continue;
            }
            String prefix = rankLabel + ".";
            int lastIncludedIndex = index;
            for (int nextIndex = index + 1; nextIndex < childCount; nextIndex++) {
                View nextChild = parent.getChildAt(nextIndex);
                if (nextChild == null || nextChild.getVisibility() != View.VISIBLE
                        || nextChild.getHeight() <= 0) {
                    continue;
                }
                int nextAdapterPosition = parent.getChildAdapterPosition(nextChild);
                if (nextAdapterPosition == RecyclerView.NO_POSITION
                        || nextAdapterPosition <= adapterPosition) {
                    continue;
                }
                String nextLabel = adapter.getRankLabelForPosition(nextAdapterPosition);
                if (nextLabel.isEmpty() || (!nextLabel.equals(rankLabel)
                        && !nextLabel.startsWith(prefix))) {
                    break;
                }
                View nextBackground = nextChild.findViewById(R.id.container_room_content_root);
                View nextTarget = nextBackground != null ? nextBackground : nextChild;
                if (nextTarget.getWidth() <= 0 || nextTarget.getHeight() <= 0) {
                    continue;
                }
                computeBoundsRelativeToRecyclerView(parent, nextTarget, childBounds);
                groupLeft = Math.min(groupLeft, childBounds.left);
                groupRight = Math.max(groupRight, childBounds.right);
                groupBottom = Math.max(groupBottom, childBounds.bottom);
                lastIncludedIndex = nextIndex;
            }
            borderPaint.setColor(resolveBorderColor(adapter.getHierarchyDepthForPosition(
                    adapterPosition)));
            groupBounds.set(groupLeft + halfStrokeWidth, groupTop + halfStrokeWidth,
                    groupRight - halfStrokeWidth, groupBottom - halfStrokeWidth);
            if (groupBounds.right > groupBounds.left && groupBounds.bottom > groupBounds.top) {
                canvas.drawRoundRect(groupBounds, cornerRadius, cornerRadius, borderPaint);
            }
        }
    }

    private void computeBoundsRelativeToRecyclerView(@NonNull RecyclerView parent,
            @NonNull View target, @NonNull RectF outRect) {
        parent.getLocationOnScreen(parentLocationOnScreen);
        target.getLocationOnScreen(childLocationOnScreen);
        float left = childLocationOnScreen[0] - parentLocationOnScreen[0];
        float top = childLocationOnScreen[1] - parentLocationOnScreen[1];
        outRect.set(left, top, left + target.getWidth(), top + target.getHeight());
    }

    private int resolveBorderColor(int depth) {
        if (borderColors.length == 0) {
            return 0;
        }
        int index = Math.max(0, Math.min(depth, borderColors.length - 1));
        return borderColors[index];
    }
}

