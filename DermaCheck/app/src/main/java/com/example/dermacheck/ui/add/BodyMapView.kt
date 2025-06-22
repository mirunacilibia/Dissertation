package com.example.dermacheck.ui.add

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.PathParser
import androidx.core.graphics.toColorInt

class BodyMapView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val regions = mutableMapOf<String, Path>()
    private val regionColors = mutableMapOf<String, Int>()
    private val animatedRegionColors = mutableMapOf<String, Int>()
    private var selectedRegion: String? = null

    var onRegionSelectedListener: ((String) -> Unit)? = null

    val defaultColor = "#D3E0EA".toColorInt()  // soft bluish-gray
    val selectedColor = "#A4BCCF".toColorInt()  // gentle highlight blue

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val outlinePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = selectedColor
        isAntiAlias = true
    }

    init {
        regions["left_hand"] = PathParser.createPathFromPathData("M 124.625 727.318 C 137.575 731.971 145.259 728.59 158.714 728.59 C 160.048 728.59 169.923 727.388 170.771 729.065 C 171.579 730.659 168.855 735.062 168.361 736.693 C 166.125 744.055 164.587 752.151 163.054 759.728 C 157.954 784.926 160.825 822.853 143.759 843.929 C 140.707 847.699 135.949 847.294 132.343 849.966 C 128.435 852.863 125.517 860.348 122.213 864.264 C 119.354 867.655 115.333 867.232 111.761 869.347 C 109.53 870.672 105.79 874.421 102.918 873.002 C 98.089 870.615 104.206 855.374 103.401 851.397 C 102.402 846.462 88.384 855 90.215 842.34 C 92.377 827.393 100.012 813.629 103.722 798.97 C 104.217 797.012 108.081 785.164 106.133 783.241 C 104.823 781.946 99.506 787.286 98.898 788.007 C 96.51 790.839 81.394 807.242 76.87 805.006 C 69.45 801.34 81.045 787.631 83.141 785.147 C 98.665 766.741 114.063 748.185 124.786 727.001")
        regionColors["left_hand"] = defaultColor

        regions["right_hand"] = PathParser.createPathFromPathData("M 567.229 718.292 C 568.764 720.718 569.273 723.804 569.853 726.956 C 570.433 730.109 571.084 733.327 572.903 736.023 C 575.727 740.212 580.042 743.479 584.511 746.658 C 588.98 749.836 593.604 752.924 597.051 756.755 C 600.632 760.736 603.416 765.127 606.073 769.588 C 608.731 774.048 611.262 778.576 614.339 782.832 C 616.189 785.392 618.378 788.037 620.364 790.806 C 622.348 793.574 624.129 796.467 625.159 799.523 C 625.434 800.333 625.529 801.91 625.262 803.345 C 624.994 804.778 624.364 806.068 623.181 806.301 C 619.2 807.089 615.25 804.493 611.893 801.01 C 608.537 797.529 605.776 793.159 604.178 790.395 C 603.755 789.665 602.61 787.728 601.3 786.062 C 599.993 784.396 598.521 783 597.447 783.354 C 594.897 784.195 594.465 787.64 594.772 791.273 C 595.081 794.905 596.124 798.722 596.525 800.304 C 597.859 805.58 599.373 810.847 600.95 816.105 C 602.527 821.364 604.169 826.615 605.76 831.858 C 606.603 834.634 607.899 837.79 608.83 841.003 C 609.761 844.216 610.331 847.49 609.72 850.502 C 608.628 855.896 604.931 855.1 601.262 853.321 C 597.592 851.541 593.947 848.779 592.961 850.241 C 592.361 851.13 593.691 855.978 594.604 860.51 C 595.518 865.043 596.015 869.26 593.753 868.887 C 589.766 868.23 587.388 866.044 585.247 863.649 C 583.107 861.253 581.208 858.649 578.181 857.152 C 577.178 856.656 576.188 857.15 575.168 857.569 C 574.149 857.99 573.1 858.334 571.979 857.544 C 570.63 856.592 569.815 855.199 569.003 853.793 C 568.192 852.388 567.38 850.973 566.041 849.981 C 562.921 847.669 559.816 845.856 556.915 843.765 C 554.015 841.677 551.32 839.313 549.017 835.899 C 546.925 832.798 545.787 829.058 545.081 825.191 C 544.376 821.327 544.102 817.337 543.738 813.735 C 542.913 805.583 541.182 797.535 539.515 789.453 C 537.848 781.373 536.248 773.256 535.689 764.969 C 535.364 760.158 534.924 755.356 534.472 750.559 C 534.021 745.759 533.556 740.963 533.181 736.154 C 533.08 734.859 532.475 732.632 532.049 730.425 C 531.624 728.218 531.376 726.029 531.994 724.81 C 532.376 724.056 534.15 723.574 536.03 723.244 C 537.91 722.915 539.896 722.74 540.704 722.595 C 543.59 722.077 549.619 722.022 555.458 721.569 C 561.295 721.116 566.946 720.267 569.075 718.162")
        regionColors["right_hand"] = defaultColor

        regions["left_arm"] = PathParser.createPathFromPathData("M 302.29 228.84 C 286.934 234.328 261.721 245.15 241.816 254.304 C 192.457 277 164.217 305.508 161.58 342.088 C 159.826 366.403 170.684 435.192 163.205 479.545 C 153.298 538.298 136.105 592.011 133.599 656.817 C 132.95 673.627 134.596 692.289 131.067 704.813 C 130.465 706.951 125.46 727.477 125.71 728.84 C 162.593 731.045 168.211 732.429 170.757 729.64 C 179.338 687.751 192.72 666.559 197.794 647.607 C 214.17 586.443 220.489 533.945 225.581 474.318 C 229.447 429.041 241.201 376.939 255.329 335.062 C 276.243 262.303 298.552 236.628 302.579 229.675")
        regionColors["left_arm"] = defaultColor

        regions["right_arm"] = PathParser.createPathFromPathData("M 398.274 229.253 C 424.785 244.56 475.038 257.713 509.428 285.085 C 547.549 315.425 541.001 372.884 537.799 429.049 C 535.786 464.323 538.885 486.615 548.209 518.862 C 561.049 563.269 574.886 613.23 573.482 659.646 C 572.477 692.898 571.149 719.605 572.267 724.578 C 573.319 729.251 540.589 729.082 534.985 726.229 C 533.044 725.241 517.337 689.06 515.865 686.515 C 503.836 665.717 504.268 651.362 498.356 627.999 C 486.932 582.851 483.263 520.284 473.94 474.233 C 463.059 389.281 451.134 353.424 433.954 314.882 C 389.576 235.492 397.753 230.221 398.402 229.606")
        regionColors["right_arm"] = defaultColor

        regions["anterior_torso"] = PathParser.createPathFromPathData("M 396.001 229.294 C 313.483 235.495 306.935 226.354 300.344 228.982 C 257.557 312.099 253.824 306.488 244.927 353.142 C 243.566 360.286 224.525 471.253 226.386 475.88 C 235.082 497.506 243.472 553.203 240.274 578.473 C 214.916 698.668 211.673 680.223 216.459 689.68 C 446.847 686.639 478.863 684.874 481.84 681.932 C 470.539 630.376 465.494 610.712 460.037 585.922 C 450.359 541.961 457.209 511.943 473.768 473.78 C 469.742 450.287 460.97 380.551 448.499 353.482 C 411.495 248.632 399.145 241.228 396.453 229.527")
        regionColors["anterior_torso"] = defaultColor

        regions["genital"] = PathParser.createPathFromPathData("M 356.168 836.726 C 353.39 837.515 353.292 814.173 348.244 818.584 C 342.374 823.714 344.392 837.505 341.78 837.73 C 222.707 828.08 210.926 839.426 210.569 838.369 C 208.371 831.858 207.46 796.884 207.459 790.128 C 208.517 733.076 209.129 700.673 216.207 688.426 C 466.884 685.938 481.13 683.028 481.519 683.54 C 481.519 683.54 484.688 706.653 485.007 708.858 C 487.806 728.218 489.96 747.72 491.191 767.189 C 492.909 816.962 488.307 836.745 488.132 838.918 C 389.49 840.103 355.353 839.242 356.187 836.731")
        regionColors["genital"] = defaultColor

        regions["left_leg"] = PathParser.createPathFromPathData("M 211.064 836.152 C 211.064 863.249 216.979 894.469 224.031 925.664 C 231.085 956.86 239.276 988.031 243.831 1015.04 C 246.456 1030.6 244.654 1047.63 242.212 1064.4 C 239.771 1081.16 238.364 1097.76 236.749 1112.18 C 233.231 1143.61 234.352 1168.86 237.573 1194.07 C 240.794 1219.28 245.388 1245.49 255.051 1275.73 C 263.096 1310.2 269.821 1343.503 266.87 1363.143 C 310.679 1374.393 310.574 1368.75 311.605 1365.76 C 318.112 1346.88 327.769 1303.36 328.737 1260.85 C 329.705 1218.34 326.802 1173.17 321.716 1139.78 C 318.117 1116.14 320.967 1089.96 324.425 1063.31 C 327.886 1036.65 331.956 1009.52 330.798 983.967 C 330.17 970.101 330.808 960.658 331.814 951.235 C 332.817 941.813 334.19 932.414 335.03 918.64 C 335.197 915.916 337.986 898.324 340.186 879.875 C 342.385 861.424 343.997 842.115 341.809 835.949 C 306.485 835.643 280.586 834.479 260.134 833.955 C 239.681 833.432 224.675 833.55 211.138 835.807")
        regionColors["left_leg"] = defaultColor

        regions["left_foot"] = PathParser.createPathFromPathData("M 266.843 1362.36 C 264.696 1379.34 254.833 1386.52 245.179 1400.34 C 239.85 1407.97 232.564 1417.54 226.316 1423.4 C 221.238 1428.17 227.686 1434.04 229.706 1438.59 C 230.973 1441.46 233.259 1444.91 233.917 1447.81 C 235.033 1452.73 241.489 1450.92 245.348 1453.87 C 247.011 1455.14 249.956 1459.35 251.965 1460.01 C 256.204 1461.4 262.136 1461.45 264.993 1458.54 C 266.467 1457.03 275.267 1471.09 282.309 1466.07 C 289.015 1461.26 296.101 1447.27 302.263 1438.01 C 308.71 1419.89 312.706 1413.1 313.171 1411.73 C 317.016 1400.42 312.895 1392.87 311.093 1382.17 C 310.851 1380.73 310.286 1369.23 309.166 1368.66 C 289.475 1368.14 277.734 1365.51 266.311 1362.65")
        regionColors["left_foot"] = defaultColor

        regions["right_leg"] = PathParser.createPathFromPathData("M 355.929 837.417 C 355.929 910.528 362.916 985.991 375.007 1057.67 C 378.663 1079.34 380.943 1111.83 376.846 1132.37 C 370.493 1164.24 381.333 1212.52 381.163 1241.14 C 381.077 1255.65 386.854 1286.83 388.107 1301.16 C 389.837 1320.93 376.882 1345.95 388.556 1356.9 C 394.409 1362.4 437.223 1355.68 433.509 1354.81 C 429.082 1353.77 434.082 1303.63 438.778 1295.15 C 444.629 1284.57 455.828 1218.09 461.373 1173.94 C 469.632 1108.18 446.742 1037.99 459.039 971.339 C 464.156 943.603 472.975 909.593 483.078 874.05 C 483.985 870.856 491.048 838.663 488.098 835.748 C 396.47 837.217 357.569 833.859 357.569 838.441")
        regionColors["right_leg"] = defaultColor

        regions["right_foot"] = PathParser.createPathFromPathData("M 389.049 1358.42 C 389.049 1362.74 389.72 1366.85 388.929 1371.54 C 387.519 1379.9 383.575 1390.39 383.785 1398.96 C 383.985 1407.06 387.481 1412.28 387.494 1412.55 C 387.773 1418.78 393.155 1428.11 396.584 1435.6 C 399.994 1443.04 407.84 1456.52 413.329 1463.37 C 416.589 1467.43 423.469 1468.56 427.084 1461.71 C 427.927 1460.12 429.342 1457.58 431.27 1457.11 C 436.543 1455.8 442.147 1460.33 447.656 1458.53 C 450.199 1457.69 451.21 1454.47 453.755 1453.2 C 455.216 1452.49 457.557 1453.7 459.018 1453.09 C 461.664 1451.98 465.977 1447.53 465.835 1445.52 C 465.501 1440.78 468.997 1441.85 470.023 1435.95 C 472.136 1423.8 468.753 1415.81 462.367 1406.76 C 456.305 1398.18 445.804 1397.25 438.087 1384.3 C 434.205 1377.8 433.59 1369.57 432.227 1362.09 C 432.061 1361.18 432.971 1354.96 432.108 1354.53 C 401.181 1357.22 389.527 1357.29 389.527 1358.78")
        regionColors["right_foot"] = defaultColor

        regions["head"] = PathParser.createPathFromPathData("M 394.894 229.106 C 326.576 234.718 304.312 231.821 302.374 229.565 C 300.112 226.933 308.851 223.934 308.975 220.033 C 309.389 207.053 310.264 194.653 305.519 184.187 C 297.404 171.115 297.441 157.024 296.892 154.955 C 296.307 152.752 291.021 156.803 288.877 153.155 C 284.737 146.112 275.882 123.086 276.501 111.81 C 276.693 108.311 283.4 110.715 284.572 112.068 C 283.17 85.392 284.282 74.61 287.695 65.709 C 298.99 36.249 321.019 22.381 347.845 19.04 C 382.344 14.743 413.72 38.789 415.421 69.883 C 416.11 82.459 416.686 99.543 413.514 110.574 C 412.784 113.115 421.299 108.532 422.147 111.073 C 424.918 119.387 416.286 144.007 412.63 151.968 C 409.128 159.59 401.108 152.303 401.904 154.798 C 403.101 158.543 395.982 180.744 393.898 184.49 C 392.757 186.538 386.748 201.877 389.472 219.322 C 387.133 223.32 398.8 229.063 395.494 229.063")
        regionColors["head"] = defaultColor

        val ellipseRect = RectF(
            351.022f - 29.896f, // left
            160.783f - 10.664f, // top
            351.022f + 29.896f, // right
            160.783f + 10.664f  // bottom
        )

        val ellipsePath = Path().apply {
            addOval(ellipseRect, Path.Direction.CW)
        }
        regions["oral"] = ellipsePath
        regionColors["oral"] = defaultColor

    regionColors.forEach { (key, color) ->
            animatedRegionColors[key] = color
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        regions.forEach { (key, path) ->
            fillPaint.color = animatedRegionColors[key] ?: regionColors[key] ?: Color.LTGRAY
            fillPaint.alpha = 255  // Fully opaque
            canvas.drawPath(path, fillPaint)

            if (key == "oral") {
                canvas.drawPath(path, outlinePaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            // Check "oral" first
            regions["oral"]?.let { oralPath ->
                val region = Region()
                val bounds = RectF()
                oralPath.computeBounds(bounds, true)
                region.setPath(
                    oralPath,
                    Region(
                        bounds.left.toInt(),
                        bounds.top.toInt(),
                        bounds.right.toInt(),
                        bounds.bottom.toInt()
                    )
                )
                if (region.contains(x.toInt(), y.toInt())) {
                    handleRegionSelection("oral")
                    return true
                }
            }

            regions.forEach { (key, path) ->
                if (key == "oral") return@forEach
                val region = Region()
                val bounds = RectF()
                path.computeBounds(bounds, true)
                region.setPath(
                    path,
                    Region(
                        bounds.left.toInt(),
                        bounds.top.toInt(),
                        bounds.right.toInt(),
                        bounds.bottom.toInt()
                    )
                )
                if (region.contains(x.toInt(), y.toInt())) {
                    handleRegionSelection(key)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleRegionSelection(key: String) {
        val previousRegion = selectedRegion
        selectedRegion = key

        if (previousRegion != key) {
            // Animate newly selected region
            val startColor = animatedRegionColors[key] ?: regionColors[key]!!
            ValueAnimator.ofArgb(startColor, selectedColor).apply {
                duration = 300
                addUpdateListener { animator ->
                    animatedRegionColors[key] = animator.animatedValue as Int
                    invalidate()
                }
                start()
            }

            // Animate previous selected region back to default
            previousRegion?.let { prevKey ->
                val start = animatedRegionColors[prevKey] ?: selectedColor
                ValueAnimator.ofArgb(start, regionColors[prevKey] ?: defaultColor).apply {
                    duration = 300
                    addUpdateListener { animator ->
                        animatedRegionColors[prevKey] = animator.animatedValue as Int
                        invalidate()
                    }
                    start()
                }
            }
        }

        onRegionSelectedListener?.invoke(key)
        performClick()
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}


