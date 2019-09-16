package com.min.baiduai.demo;

import android.text.TextUtils;

class CompareRank {
    static final int GAME_WIN = 2;
    static final int RANK_WIN = 1;
    static final int RANK_DRAW = 0;
    static final int RANK_LOSE = -1;
    static final int GAME_LOSE = -2;
    static final int INVALID = Integer.MAX_VALUE;

    private final String[] CHESS = {
            "旗","司", "令","军","师","旅","团","营","连","排","长","工","兵","地","雷","炸","弹"
    };

    private final String[] RANK = {
            "军旗","司令","军长","师长","旅长","团长","营长","连长","排长","工兵"
    };

    private final String[] WEAPON = {
            "地雷", "炸弹"
    };

    CompareRank() {
    }

    int compare(String content) {
        String filterStr = filterChessCharacter(content);

        String[] ranks = getBothRanks(filterStr);
        String leftRank = ranks[0];
        String rightRank = ranks[1];
        if (TextUtils.isEmpty(leftRank) || TextUtils.isEmpty(rightRank)) return INVALID;

        if (isBoss(leftRank)) return GAME_LOSE;
        if (isBoss(rightRank)) return GAME_WIN;

        if (isBomb(leftRank) || isBomb(rightRank)) return RANK_DRAW;

        int redIndex = getRankIndex(leftRank);
        int blackIndex = getRankIndex(rightRank);

        if (isMine(leftRank)){
            if (RANK.length - 1 == blackIndex) return RANK_LOSE;
            else return RANK_WIN;
        }

        if (isMine(rightRank)) {
            if (RANK.length - 1 == redIndex) return RANK_WIN;
            else return RANK_LOSE;
        }

        return Integer.compare(blackIndex, redIndex);
//        return (redIndex < blackIndex)? RANK_WIN : ((redIndex > blackIndex)? RANK_LOSE : RANK_DRAW);
    }

    private String filterChessCharacter(String content) {
        StringBuilder filterStr = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            for (String chess : CHESS) {
                if (chess.equals(content.substring(i, i + 1))) {
                    filterStr.append(content.substring(i, i + 1));
                    break;
                }
            }
        }

        return filterStr.toString();
    }

    private boolean isValid(String rank) {
        if (getRankIndex(rank) >= 0) return true;
        return (isBomb(rank) || isMine(rank));
    }

    private int getRankIndex(String rank) {
        for (int i = 0; i < RANK.length; i++) {
            if (RANK[i].equals(rank)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isMine(String weapon) {
        return WEAPON[0].equals(weapon);
    }

    private boolean isBomb(String weapon) {
        return WEAPON[1].equals(weapon);
    }

    private boolean isBoss(String rank) {
        return RANK[0].equals(rank);
    }

    private String[] splitStr(String content) {
        final int splitLen = 2;
        int count = content.length() / splitLen + (content.length() % splitLen == 0 ? 0 : 1);
        String[] strArrays = new String[count];
        for (int i = 0; i < count; i++) {
            if (content.length() <= splitLen) {
                strArrays[i] = content;
            } else {
                strArrays[i] = content.substring(0, splitLen);
                content = content.substring(splitLen);
            }
        }
        return strArrays;
    }

    private String[] getBothRanks(String content) {
        String[] ranks = splitStr(content);

        String rankRed = null, rankBlack = null;
        for (String rank : ranks) {
            if (isValid(rank)) {
                if (TextUtils.isEmpty(rankRed)) {
                    rankRed = rank;
                } else {
                    rankBlack = rank;
                    break;
                }
            }
        }

        return new String[] {rankRed, rankBlack};
    }
}
