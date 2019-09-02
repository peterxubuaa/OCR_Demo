package com.min.tesserocrdemo;

class CompareRank {
    static final int WIN = 1;
    static final int DRAW = 0;
    static final int LOSE = -1;

    private static final String[] CHESS = {
            "旗","司", "令","军","师","旅","团","营","连","排","长","工","兵","地","雷","炸","弹"
    };

    private static final String[] RANK = {
            "军旗","司令","军长","师长","旅长","团长","营长","连长","排长","工兵"
    };

    private static final String[] WEAPON = {
            "地雷", "炸弹"
    };

    static String filterChessCharacter(String content) {
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

    static boolean isValid(String rank) {
        if (getRankIndex(rank) >= 0) return true;
        return (isBomb(rank) || isMine(rank));
    }

    /*
     0: 平局
     1： 赢
     -1：输
     */
    static int compare(String leftRank, String rightRank) {
        if (isBoss(leftRank)) return LOSE;
        if (isBoss(rightRank)) return WIN;

        if (isBomb(leftRank) || isBomb(rightRank)) return DRAW;

        int redIndex = getRankIndex(leftRank);
        int blackIndex = getRankIndex(rightRank);

        if (isMine(leftRank)){
            if (RANK.length - 1 == blackIndex) return LOSE;
            else return WIN;
        }

        if (isMine(rightRank)) {
            if (RANK.length - 1 == redIndex) return WIN;
            else return LOSE;
        }

        return (redIndex < blackIndex)? WIN : ((redIndex > blackIndex)? LOSE : DRAW);
    }

    static boolean isGameOver(String leftRank, String rightRank) {
        return (isBoss(leftRank) || isBoss(rightRank));
    }

    private static int getRankIndex(String rank) {
        for (int i = 0; i < RANK.length; i++) {
            if (RANK[i].equals(rank)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isMine(String weapon) {
        return WEAPON[0].equals(weapon);
    }

    private static boolean isBomb(String weapon) {
        return WEAPON[1].equals(weapon);
    }

    private static boolean isBoss(String rank) {
        return RANK[0].equals(rank);
    }
}
