package com.company.circlethecat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Pair;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Board extends SurfaceView {
    final SurfaceHolder mSurfaceHolder;
    static int mBoardEdgeSize;
    Boolean[][] mBoardContent;
    private Queue<Pair> mTouchQueue;
    private final Lock mLock;
    private Cat mCat;
    private final Activity mActivity;
    private final Bitmap mCatBitmap;

    private Boolean mIsRunning = false;
    private Thread mThread;
    private Boolean mHasWon = false;

    public Board(Activity activity, Bitmap catBitmap, int BoardSize) {
        super(activity);
        mBoardEdgeSize = BoardSize;
        mBoardContent = new Boolean[mBoardEdgeSize][];
        for (int i = 0; i < mBoardEdgeSize; i++) {
            mBoardContent[i] = new Boolean[mBoardEdgeSize];
            for (int j = 0; j < mBoardEdgeSize; j++) {
                mBoardContent[i][j] = false;
            }
        }
        mActivity = activity;
        mSurfaceHolder = getHolder();
        mTouchQueue = new LinkedList<>();
        mLock = new ReentrantLock(true);
        mCatBitmap = catBitmap;
        initializeBoard(BoardSize);
    }

    private void initializeBoard(int BoardSize) {
        mCat = new Cat(mCatBitmap, BoardSize);
        Random r = new Random();
        for (int i = 0; i < mBoardEdgeSize; i++) {
            mBoardContent[r.nextInt(mBoardEdgeSize)][r.nextInt(mBoardEdgeSize)] = true;
        }
        mBoardContent[BoardSize/2][BoardSize/2] = false;
    }

    public void resetBoard() {
        pause();
        mTouchQueue = new LinkedList<>();
        mHasWon = false;
        for (int i = 0; i < mBoardEdgeSize; i++) {
            for (int j = 0; j < mBoardEdgeSize; j++) {
                mBoardContent[i][j] = false;
            }
        }
        initializeBoard(mBoardEdgeSize);
        resume();
    }

    public void addTouchEvent(float x, float y) {
        mLock.lock();
        mTouchQueue.add(new Pair(x, y));
        mLock.unlock();
        synchronized (mRenderUI) {
            mRenderUI.notify();
        }
    }

    private Runnable mResetActivity = new Runnable() {
        @Override
        public void run() {
            resetBoard();
        }
    };

    private Runnable mRenderUI = new Runnable() {
        @Override
        public void run() {
            while (mIsRunning) {
                if ((mCat.hasEscaped() || mHasWon) && !mCat.isAnimating()) {
                    if(mCat.hasEscaped())
                        mActivity.runOnUiThread(new Runnable() {
                                               public void run() {
                                                   Toast toast = new Toast(mActivity);
                                                   ImageView view = new ImageView(mActivity);
                                                   view.setImageResource(R.drawable.cat);
                                                   toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                                   toast.setDuration(Toast.LENGTH_LONG);
                                                   toast.setView(view);
                                                   toast.show();
                                                   Toast.makeText(mActivity, "Ты проиграл:(", Toast.LENGTH_SHORT).show();
                                               }
                                           }
                        );
                    if(mHasWon)
                        mActivity.runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        Toast toast = new Toast(mActivity);
                                                        ImageView view = new ImageView(mActivity);
                                                        view.setImageResource(R.drawable.smile_cat);
                                                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                                                        toast.setDuration(Toast.LENGTH_LONG);
                                                        toast.setView(view);
                                                        toast.show();
                                                        Toast.makeText(mActivity, "Ты победил:)", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                        );

                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    (new Handler(mActivity.getMainLooper())).post(mResetActivity);
                    return;
                }

                if (!mSurfaceHolder.getSurface().isValid()) {
                    continue;
                }

                Canvas canvas = mSurfaceHolder.lockCanvas();
                float trans = (Math.max(canvas.getWidth(), canvas.getHeight()) - Math.min(canvas.getWidth(), canvas.getHeight())) / 2;
                float xTrans, yTrans;
                if (canvas.getHeight() > canvas.getWidth()) {
                    xTrans = 0;
                    yTrans = trans;
                } else {
                    xTrans = trans;
                    yTrans = 0;
                }
                if (canvas.getHeight() > canvas.getWidth()) {
                    canvas.translate(xTrans, yTrans);
                    canvas.drawARGB(255,255,255,255);
                    if (!mCat.isAnimating()) {
                        processTouchEvents(canvas, xTrans, yTrans);
                    }
                    for (int i = 0; i < mBoardContent.length; i++) {
                        for (int j = 0; j < mBoardContent[0].length; j++) {
                            drawCircle(canvas, i, j, mBoardContent[i][j]);
                        }
                    }
                    mCat.draw(canvas);
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                    if (mCat.isAnimating()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    synchronized (mRenderUI) {
                        if (mIsRunning && !mCat.isAnimating() && !mCat.hasEscaped() && !mHasWon) {
                            try {
                                mRenderUI.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    };

    private void processTouchEvents(Canvas canvas, float xTrans, float yTrans) {
        float boardSize = Math.min(canvas.getWidth(), canvas.getHeight());
        float rectBoundSize = (float) (boardSize / ((float) mBoardEdgeSize + 0.5));
        mLock.lock();
        while (!mTouchQueue.isEmpty()) {
            Pair p = mTouchQueue.remove();
            float x = (float) p.first;
            float y = (float) p.second;
            x -= xTrans;
            y -= yTrans;

            int i;
            int j = (int) ((y / rectBoundSize));
            if (j % 2 == 1) {
                i = (int) (((x - rectBoundSize / 2) / rectBoundSize));
            } else {
                i = (int) ((x / rectBoundSize));
            }

            if (i < 0 || i >= mBoardEdgeSize || j < 0 || j >= mBoardEdgeSize) {
                continue;
            }

            Pair catPos = mCat.position();
            if (mBoardContent[i][j] || ((int) catPos.first == i && (int) catPos.second == j)) {
                continue;
            }
            mBoardContent[i][j] = true;
            BoardAI ai = new BoardAI(mBoardContent, (int) catPos.first, (int) catPos.second);
            int move = ai.nextMove();
            if (move >= 0) {
                mCat.move(move);
            } else if (move == -2) {
                mCat.escape();
            } else if (move == -1) {
                mHasWon = true;
            }
        }
        mLock.unlock();
    }

    private void drawCircle(Canvas canvas, int x, int y, Boolean isSelected) {
        float boardSize = Math.min(canvas.getWidth(), canvas.getHeight());
        float rectBoundSize = (float) (boardSize / ((float) mBoardEdgeSize + 0.5));
        float centreX = x * rectBoundSize + (rectBoundSize / 2);
        float centreY = y * rectBoundSize + (rectBoundSize / 2);
        if (y % 2 == 1) {
            centreX += (rectBoundSize / 2);
        }
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        if (isSelected) {
            paint.setColor(Color.WHITE);
            canvas.drawCircle(centreX, centreY, rectBoundSize / 2, paint);
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.fire);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(b, (int)rectBoundSize, (int)rectBoundSize, false);

            canvas.drawBitmap(resizedBitmap, centreX - rectBoundSize / 2, centreY - rectBoundSize / 2 , null);
        } else {
            //todo
            paint.setColor(getResources().getColor(R.color.dot_color));
            canvas.drawCircle(centreX, centreY, rectBoundSize / 2, paint);
        }

    }

    public void pause() {
        synchronized (mRenderUI) {
            mIsRunning = false;
            mRenderUI.notify();
        }
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        mIsRunning = true;
        mThread = new Thread(mRenderUI);
        mThread.start();
    }
}
