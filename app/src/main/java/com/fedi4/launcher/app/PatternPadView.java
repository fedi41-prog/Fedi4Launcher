package com.fedi4.launcher.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.BlurMaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.core.content.ContextCompat;

import com.fedi4.launcher.R;

import java.util.ArrayList;

public class PatternPadView extends View {

    private static final int GRID = 3;
    private static final float LINE_WIDTH = 100f;

    private Paint circlePaint, glowPaint, linePaint, glowLinePaint;
    private PointF[] centers;
    private float radius;
    private ArrayList<Integer> selected = new ArrayList<>();
    private float lastX = -1, lastY = -1;
    private Path linePath = new Path();
    private TouchListener listener;

    private int lastHit = -1;

    // Animation
    private float[] scaleFactors;

    // Icon support: Bitmap pro Punkt (null = kein Icon)
    private Bitmap[] pointIcons = new Bitmap[GRID * GRID];

    public PatternPadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null); // für Glow
        init();
    }

    // Speichert die Linien mit "Ablaufzeit"
    private static class TrailSegment {
        PointF start, end;
        long expireAt;
    }

    private ArrayList<TrailSegment> trails = new ArrayList<>();
    private static final long TRAIL_LIFETIME = 0; // ms sichtbar
    private static final long TRAIL_FADE = 500;     // ms zum Ausfaden


    private void init() {
        // Standard Kreis
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(5f);
        circlePaint.setColor(ContextCompat.getColor(getContext(), R.color.dot_outline_color));

        // Glow Effekt für Punkte
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setColor(ContextCompat.getColor(getContext(), R.color.dot_color)); // Cyan Neon
        glowPaint.setMaskFilter(new BlurMaskFilter(30, BlurMaskFilter.Blur.NORMAL));

        // Linien (sichtbar)
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(LINE_WIDTH);
        linePaint.setColor(ContextCompat.getColor(getContext(), R.color.line_color));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        // Glow für Linien
        glowLinePaint = new Paint(linePaint);
        glowLinePaint.setMaskFilter(new BlurMaskFilter(25, BlurMaskFilter.Blur.NORMAL));
    }

    public void setTouchListener(TouchListener listener) {
        this.listener = listener;
    }

    /* ---------------------- Layout / Centers ---------------------- */

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        generateCenters(w, h);
    }

    private void generateCenters(int w, int h) {
        centers = new PointF[GRID * GRID];
        scaleFactors = new float[GRID * GRID];
        for (int i = 0; i < scaleFactors.length; i++) scaleFactors[i] = 1f;

        float margin = Math.min(w, h) * 0.2f;
        float usableW = w - 2 * margin;
        float usableH = h - 2 * margin;
        float stepX = usableW / (GRID - 1);
        float stepY = usableH / (GRID - 1);
        int idx = 0;
        for (int y = 0; y < GRID; y++) {
            for (int x = 0; x < GRID; x++) {
                centers[idx++] = new PointF(margin + x * stepX, margin + y * stepY);
            }
        }
        radius = Math.min(stepX, stepY) * 0.33f;
    }

    /* ---------------------- Draw ---------------------- */

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = System.currentTimeMillis();

        // ============================================================

        // 👉 Trail Segmente zeichnen
        ArrayList<TrailSegment> toRemove = new ArrayList<>();
        for (TrailSegment seg : trails) {
            float alpha = 1f;
            long timeLeft = seg.expireAt - now;
            if (timeLeft <= 0) {
                toRemove.add(seg);
                continue;
            }
            if (timeLeft < TRAIL_FADE) {
                alpha = (float) timeLeft / (float) TRAIL_FADE;
            }

            int col = (int) (alpha * 255);
            linePaint.setAlpha(col);
            glowLinePaint.setAlpha(col);

            canvas.drawLine(seg.start.x, seg.start.y, seg.end.x, seg.end.y, glowLinePaint);
            canvas.drawLine(seg.start.x, seg.start.y, seg.end.x, seg.end.y, linePaint);
        }
        trails.removeAll(toRemove);
        // 👉 aktueller Fingerzug (noch nicht gespeichert)
        if (lastHit != -1 && lastX != -1 && lastY != -1) {
            PointF lastPoint = getCenterCoordinatesForPoint(lastHit);
            canvas.drawLine(lastPoint.x, lastPoint.y, lastX, lastY, glowLinePaint);
            canvas.drawLine(lastPoint.x, lastPoint.y, lastX, lastY, linePaint);
        }

        // =========================================================

        // Declare this outside the loop, or as a member variable (e.g., private RectF mBitmapDrawRect = new RectF();)
        RectF bitmapDrawRect = new RectF();

        // 👉 Punkte zeichnen (korrigiert)
        for (int i = 0; i < centers.length; i++) {
            PointF c = centers[i];
            float scale = scaleFactors[i];
            Bitmap icon = pointIcons[i]; // 'icon' is a Bitmap

            if (icon != null) {
                int size = (int) (radius * 2 * scale);
                int left = (int) (c.x - size / 2f);
                int top = (int) (c.y - size / 2f);
                int right = left + size;
                int bottom = top + size;

                // Canvas auf Kreis beschränken
                int save = canvas.save();
                Path circlePath = new Path();
                circlePath.addCircle(c.x, c.y, radius * scale, Path.Direction.CW);
                canvas.clipPath(circlePath);

                // Bitmap skalieren und zeichnen
                Rect src = new Rect(0, 0, icon.getWidth(), icon.getHeight());
                Rect dst = new Rect(left, top, right, bottom);
                canvas.drawBitmap(icon, src, dst, null);

                // Clip zurücksetzen
                canvas.restoreToCount(save);

                // Optional Rand des Kreises zeichnen
                canvas.drawCircle(c.x, c.y, radius * scale, circlePaint);
            } else {
                canvas.drawCircle(c.x, c.y, radius * scale, circlePaint);
            }

        }

        // ============================================================

        // ständiges Redraw, solange Trails leben
        if (!trails.isEmpty()) postInvalidateOnAnimation();
    }


    /* ---------------------- Touch / Detection ---------------------- */

    private int findHitNode(float x, float y) {
        for (int i = 0; i < centers.length; i++) {
            PointF p = centers[i];
            float dx = x - p.x;
            float dy = y - p.y;
            if (dx * dx + dy * dy <= radius * radius) return i;
        }

        return -1;
    }
    private PointF getCenterCoordinatesForPoint(int pointId) {
        // Überprüfen, ob die 'centers' initialisiert wurden und die pointId gültig ist
        if (centers != null && pointId >= 0 && pointId < centers.length) {
            // Direkter Zugriff auf das PointF-Objekt im Array
            PointF center = centers[pointId];
            // Optional: Eine neue Instanz zurückgeben, um die interne Instanz vor externer Modifikation zu schützen.
            // Für die meisten Anwendungsfälle ist die direkte Rückgabe der Referenz in Ordnung und performanter.
            // return new PointF(center.x, center.y); // Wenn Kapselung des internen Objekts gewünscht ist
            return center;
        } else {
            // Loggen Sie einen Fehler oder eine Warnung, wenn die ID ungültig ist oder die Zentren nicht initialisiert sind
            if (centers == null) {
                Log.w("PatternPadView", "getCenterCoordinatesForPoint: 'centers' array is not initialized yet.");
            } else {
                Log.w("PatternPadView", "getCenterCoordinatesForPoint: Invalid pointId: " + pointId + ". Must be between 0 and " + (centers.length - 1));
            }
            return null; // Ungültige ID oder Zentren nicht bereit
        }
    }

    private void animatePoint(int idx) {
        ValueAnimator anim = ValueAnimator.ofFloat(1f, 1.35f, 1.4f, 1.35f, 1f);
        anim.setDuration(200);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(valueAnimator -> {
            scaleFactors[idx] = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        anim.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                int hitDown = findHitNode(lastX, lastY);
                if (hitDown != -1 && hitDown != lastHit) {
                    listener.onTouchDetected(hitDown);
                    animatePoint(hitDown);
                    lastHit = hitDown;
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                int hitMove = findHitNode(x, y);

                if (hitMove != -1 && hitMove != lastHit) {
                    listener.onTouchDetected(hitMove);
                    animatePoint(hitMove);

                    PointF prev = getCenterCoordinatesForPoint(lastHit);
                    PointF cur = getCenterCoordinatesForPoint(hitMove);

                    if (prev != null && cur != null) {
                        addTrail(prev, cur);
                    }
                    lastHit = hitMove;
                }

                lastX = x;
                lastY = y;

                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Finger los: aktuellen Zug auch als Trail sichern

                float xx = event.getX();
                float yy = event.getY();
                int hitEnd = findHitNode(xx, yy);

                if (listener != null) {
                    Log.d("PatternPad", "confirmed pattern ----");
                    listener.onTouchEnded(hitEnd);
                    lastHit = -1;
                }

                lastX = lastY = -1;

                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }


    /* ---------------------- Icon-API (öffentlich) ---------------------- */

    /** Setze ein Bitmap-Icon für Punkt idx (0..8). */
    public void setPointIconBitmap(int idx, Bitmap bmp) {
        if (idx < 0 || idx >= pointIcons.length) return;
        pointIcons[idx] = bmp;
        invalidate();
    }

    /** Setze ein Drawable-Icon für Punkt idx (konvertiert intern). */
    public void setPointIconDrawable(int idx, Drawable d) {
        if (d == null) {
            clearPointIcon(idx);
            return;
        }
        setPointIconBitmap(idx, drawableToBitmap(d));
    }

    /** Entferne Icon für Punkt idx */
    public void clearPointIcon(int idx) {
        if (idx < 0 || idx >= pointIcons.length) return;
        pointIcons[idx] = null;
        invalidate();
    }

    /** Setze alle Icons (Array mit length 9). null erlaubt. */
    public void setAllPointIcons(Bitmap[] icons) {
        if (icons == null) return;
        for (int i = 0; i < Math.min(icons.length, pointIcons.length); i++) {
            pointIcons[i] = icons[i];
        }
        invalidate();
    }

    /* ---------------------- Hilfsmethoden ---------------------- */

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            Bitmap b = ((BitmapDrawable) drawable).getBitmap();
            if (b != null) return b;
        }
        // Erzeuge Bitmap in knappem Quadrat
        int w = drawable.getIntrinsicWidth() <= 0 ? (int) (radius * 2) : drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight() <= 0 ? (int) (radius * 2) : drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public interface TouchListener {
        void onTouchDetected(int point);

        void onTouchEnded(int point);
    }

    private void addTrail(PointF start, PointF end) {
        TrailSegment seg = new TrailSegment();
        seg.start = new PointF(start.x, start.y);
        seg.end = new PointF(end.x, end.y);
        seg.expireAt = System.currentTimeMillis() + TRAIL_LIFETIME + TRAIL_FADE;
        trails.add(seg);
    }

}
