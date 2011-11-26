#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <math.h>
#include "board.h"
#include "com.h"
#include "evaluator.h"
#include "opening.h"

#define BUFFER_SIZE 64
#define MPC_NUM 10
#define MPC_FILE "mpc.dat"
#define MPC_LEARN_FILE "mpc_learn.dat"
#define EVALUATOR_FILE "eval.dat"
#define EVALUATOR_FILE_2 "eval_20111119.dat"
#define GAME_INFO_FILE "game_info.dat"
#define OPENING_TRANSCRIPT_FILE "open_trans.txt"
#define OPENING_FILE "open.dat"
#define TRANSCRIPT_SIZE 128
struct _MainParam
{
	Board *Board;
	Evaluator *Evaluator;
	Evaluator *Evaluator2;
	Opening *Opening;
	Com *Com;
	Com *Com2;
	Com *Com3;
};
typedef struct _MainParam MainParam;

static int get_rand(int in_max);
static int move_random(Board *board, int in_color);
static char * get_stream(char *out_buffer, int in_size, FILE *stream);
static void print_board(const Board *in_board);
static void play(Board *board, Com *com);
static void learn(Board *board, Evaluator *evaluator, Com *com);
static int main_param_initialize_each(MainParam *self);
static int main_param_initialize(MainParam *self);
static void main_param_finalize(MainParam *self);
static void opening_initialize(Board *iboard, Opening *opening);
static void calc_mpc(Board *board, Com *com);
static int write_mpc(FILE *fp, int in_num, const MPCInfo *in_info);
static int save_mpc(int in_array_num, int *in_num, const MPCInfo **in_info);
#define array_num(array) (sizeof(array)/sizeof(array[0]))

static int get_rand(int in_max)
{
	return (int)((double)in_max * rand() / (RAND_MAX + 1.0));
}

static int move_random(Board *board, int in_color)
{
	int pos;
	while (1) {
		pos = Board_Pos(get_rand(BOARD_SIZE), get_rand(BOARD_SIZE));
		if (Board_Flip(board, in_color, pos)) {
			return pos;
		}
	}
}

static char * get_stream(char *out_buffer, int in_size, FILE *stream)
{
	char *result;
	int i;

	result = fgets(out_buffer, in_size, stream);
	if (result != NULL) {
		for (i = 0; i < in_size; i++) {
			if (out_buffer[i] == '\r' || out_buffer[i] == '\n') {
				out_buffer[i] = '\0';
			}
		}
	}
	return result;
}

static void print_board(const Board *in_board)
{
	int x, y;
	printf("  A B C D E F G H\n");
	for (y = 0; y < BOARD_SIZE; y++) {
		printf("%d ", y+1);
		for (x = 0; x < BOARD_SIZE; x++) {
			switch (Board_Disk(in_board, Board_Pos(x, y))) {
			case BLACK:
				printf("O ");
				break;
			case WHITE:
				printf("X ");
				break;
			case EMPTY:
				printf(". ");
				break;
			default:
				printf("# ");
				break;
			}
		}
		printf("\n");
	}
	printf("O %2d - X %2d\n", Board_CountDisks(in_board, BLACK), Board_CountDisks(in_board, WHITE));
	printf("\n");
}

static void play(Board *board, Com *com)
{
	char buffer[BUFFER_SIZE];
	int history[BOARD_SIZE * BOARD_SIZE * 2];
	int color = BLACK;
	int player_color;
	int move;
	int score;
	clock_t clock_start, clock_end;
	int n, x, y;

	for (n = 0; n < BOARD_SIZE * BOARD_SIZE * 2; n++) {
		history[n] = NOMOVE;
	}
	Board_Clear(board);

	n = 0;
	while (1) {
		printf("あなたの色を選択してください (1:黒 2:白)\n");
		get_stream(buffer, BUFFER_SIZE, stdin);
		if (!strcmp(buffer, "1")) {
			player_color = BLACK;
			break;
		} else if (!strcmp(buffer, "2")) {
			player_color = WHITE;
			break;
		}
	}
	while (1) {
		printf("コンピュータのレベルを選択してください (1-4)\n");
		get_stream(buffer, BUFFER_SIZE, stdin);
		if (!strcmp(buffer, "1")) {
			Com_SetLevel(com, 2, 8, 10);
			break;
		} else if (!strcmp(buffer, "2")) {
			Com_SetLevel(com, 4, 10, 12);
			break;
		} else if (!strcmp(buffer, "3")) {
			Com_SetLevel(com, 6, 12, 14);
			break;
		} else if (!strcmp(buffer, "4")) {
			Com_SetLevel(com, 8, 14, 16);
			break;
		}
	}
	Com_SetOpening(com, 0);
	Com_LoadMPCInfo(com, MPC_FILE);
	Com_SetRandom(com, 0, 0);
	while (1) {
		print_board(board);
		if (!Board_CanPlay(board, BLACK) && !Board_CanPlay(board, WHITE)) {
			if (Board_CountDisks(board, player_color) == 0) {
				score = -BOARD_SIZE * BOARD_SIZE;
			} else if (Board_CountDisks(board, Board_OpponentColor(player_color)) == 0) {
				score = BOARD_SIZE * BOARD_SIZE;
			} else {
				score = Board_CountDisks(board, player_color) - Board_CountDisks(board, Board_OpponentColor(player_color));
			}
			if (score > 0) {
				printf("あなたの%d石勝ちです\n", score);
			} else if (score < 0) {
				printf("コンピュータの%d石勝ちです\n", -score);
			} else {
				printf("引き分けです\n");
			}
			break;
		}
		if (color == player_color) {
			printf("あなたの番です、次の手を入力してください\n");
			get_stream(buffer, BUFFER_SIZE, stdin);
			if (!strcmp(buffer, "q") || !strcmp(buffer, "quit")) {
				printf("ゲームを中断します\n");
				break;
			} else if (!strcmp(buffer, "u") || !strcmp(buffer, "undo")) {
				if (n > 1) {
					n--;
					if (history[n] != PASS) {
						Board_Unflip(board);
					}
					n--;
					if (history[n] != PASS) {
						Board_Unflip(board);
					}
				}
			} else if (!strcmp(buffer, "p") || !strcmp(buffer, "pass")) {
				if (!Board_CanPlay(board, color) && Board_CanPlay(board, Board_OpponentColor(color))) {
					color = Board_OpponentColor(color);
					history[n] = PASS;
					n++;
				} else {
					printf("パスはできません\n");
				}
			} else if (strlen(buffer) != 2) {
				printf("無効なコマンドです\n");
			} else {
				x = tolower(buffer[0]) - 'a';
				y = buffer[1] - '1';
				if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
					printf("無効なコマンドです\n");
				} else if (Board_Flip(board, color, Board_Pos(x, y))) {
					color = Board_OpponentColor(color);
					history[n] = Board_Pos(x, y);
					n++;
				} else {
					printf("そこには置けません\n");
				}
			}
		} else {
			printf("コンピュータ思考中...\n");
			if (Board_CanPlay(board, color)) {
				clock_start = clock();
				move = Com_NextMove(com, board, color, &score);
				clock_end = clock();
				printf("%c%cに置きます\n", "ABCDEFGH"[Board_X(move)], "12345678"[Board_Y(move)]);
				printf("評価値: %.2f\n", (double)score / DISK_VALUE);
				printf("思考時間: %.2f 秒 ノード数: %d NPS: %.2f knps\n", (double)(clock_end - clock_start) / CLOCKS_PER_SEC, 
					Com_CountNodes(com), (double)Com_CountNodes(com) / (clock_end - clock_start + 1) * CLOCKS_PER_SEC / 1000);
				if (Com_CountHashGet(com) > 0) {
					printf("ヒット率 %.2f %%\n", (double)Com_CountHashHit(com) / Com_CountHashGet(com) * 100);
				} else {
					printf("ヒット率 %.2f %%\n", 0.0);
				}
				Board_Flip(board, color, move);
			} else {
				printf("パスします\n");
				move = PASS;
			}
			color = Board_OpponentColor(color);
			history[n] = move;
			n++;
		}
		printf("\n");
	}
}

static void learn(Board *board, Evaluator *evaluator, Com *com)
{
	char buffer[BUFFER_SIZE];
	int history_color[BOARD_SIZE * BOARD_SIZE];
	int history_value[BOARD_SIZE * BOARD_SIZE];
	int lambda = 977;
	int i, j, move, num, turn, value;
	int color;
	int result;

	printf("対戦回数を入力してください\n");
	get_stream(buffer, BUFFER_SIZE, stdin);
	num = atoi(buffer);

	Com_SetLevel(com, 4, 12, 12);
	Com_SetRandom(com, 2, 8);
	Com_SetOpening(com, 0);
	Com_LoadMPCInfo(com, MPC_LEARN_FILE);
	for (i = 0; i < num; i++) {
		Board_Clear(board);
		color = BLACK;
		turn = 0;
		for (j = 0; j < 8; j++) {
			if (Board_CanPlay(board, color)) {
				move_random(board, color);
				history_color[turn] = color;
				turn++;
			}
			color = Board_OpponentColor(color);
		}
		while (1) {
			if (Board_CanPlay(board, color)) {
				if (Board_CountDisks(board, EMPTY) > 12 && get_rand(100) < 1) {
					move_random(board, color);
					history_value[turn] = 0;
					history_color[turn] = EMPTY;
				} else {
					move = Com_NextMove(com, board, color, &value);
					Board_Flip(board, color, move);
					history_value[turn] = value;
					history_color[turn] = color;
				}
				turn++;
			} else if (!Board_CanPlay(board, Board_OpponentColor(color))) {
				break;
			}
			color = Board_OpponentColor(color);
		}
		result = (Board_CountDisks(board, BLACK) - Board_CountDisks(board, WHITE)) * DISK_VALUE;
		for (j = Board_CountDisks(board, EMPTY); j < 8; j++) {
			turn--;
			Board_Unflip(board);
		}
		for (j = Board_CountDisks(board, EMPTY); j < BOARD_SIZE * BOARD_SIZE - 12; j++) {
			turn--;
			Board_Unflip(board);
			Evaluator_SetStage(evaluator, Board_CountDisks(board, EMPTY));
			if (history_color[turn] == BLACK) {
				result = (result * lambda + history_value[turn] * (1000 - lambda)) / 1000;
				Evaluator_Add(evaluator, board, result);
			} else if (history_color[turn] == WHITE) {
				result = (result * lambda - history_value[turn] * (1000 - lambda)) / 1000;
				Board_Reverse(board);
				Evaluator_Add(evaluator, board, -result);
				Board_Reverse(board);
			} else {
				if (turn > 0) {
					if (history_color[turn - 1] == BLACK) {
						result = history_value[turn - 1];
					} else if (history_color[turn - 1] == WHITE) {
						result = -history_value[turn - 1];
					} else {
						result = 0;
					}
				}
			}
		}
		if ((i + 1) % 10 == 0) {
			Evaluator_Update(evaluator);
		}
		if ((i + 1) % 100 == 0) {
			printf("学習中... %d / %d\r", i + 1 , num );
			Evaluator_Save(evaluator, EVALUATOR_FILE);
		}
	}
	Evaluator_Save(evaluator, EVALUATOR_FILE);
	printf("終了しました                    \n");
}

struct _GameInfo
{
	char Moves[BOARD_SIZE * BOARD_SIZE - 4]; /* パスは含めない */
	int Values[BOARD_SIZE * BOARD_SIZE - 4]; /* 黒から見た評価値 */
};
typedef struct _GameInfo GameInfo;

static void self_play(Board *board, Evaluator *evaluator, Com *com, const char *in_file_name)
{
	int i, j, move, turn, value;
	int color;
	GameInfo info;
	FILE *fp;
	const int num = 300000;

	fp = fopen(in_file_name, "wb");
//	Com_SetLevel(com, 4, 12, 12);
//	Com_SetLevel(com, 6, 14, 14);
	Com_SetLevel(com, 8, 16, 16);
	Com_SetRandom(com, 1, 8);
	Com_SetOpening(com, 0);
	Com_LoadMPCInfo(com, MPC_LEARN_FILE);
	for (i = 0; i < num; i++) {
		Board_Clear(board);
		memset(&info, 0, sizeof(info));
		color = BLACK;
		turn = 0;
		for (j = 0; j < 8; j++) {
			if (Board_CanPlay(board, color)) {
				info.Moves[turn] = (char)move_random(board, color);
				info.Values[turn] = 0;
				turn++;
			}
			color = Board_OpponentColor(color);
		}
		while (1) {
			if (Board_CanPlay(board, color)) {
				move = Com_NextMove(com, board, color, &value);
				Board_Flip(board, color, move);
				info.Moves[turn] = move;
				if (color == BLACK) {
					info.Values[turn] = value;
				} else {
					info.Values[turn] = -value;
				}
				turn++;
			} else if (!Board_CanPlay(board, Board_OpponentColor(color))) {
				break;
			}
			color = Board_OpponentColor(color);
		}
		fwrite(&info, sizeof(info), 1, fp);
		fflush(fp);
		if ((i + 1) % 100 == 0) {
			printf("対局中... %d / %d\r", i + 1 , num );
			fflush(fp);
		}
	}
	fclose(fp);
	printf("終了しました                    \n");
}

struct _LevelInfo {
	int middle;
	int exact;
	int wld;
	int random;
	int ratio;
};
typedef struct _LevelInfo LevelInfo;

struct _CompareResult {
	int win;
	int lose;
	int draw;
	int rating;
	int rating_diff;
	int rating_count;
	int rating_sum;
	int average;
};
typedef struct _CompareResult CompareResult;

static void compare_com(Board *board, Evaluator *evaluator, Com *com, Com *com2)
{
	int i, j, d, round;
	int move, value;
	int moved;
	int count;
	const int round_max = 40;
	LevelInfo info[] = {
#if 1
		{2, 4, 4, 4, 45},
		{2, 4, 4, 4, 40},
		{2, 4, 4, 3, 40},
		{2, 4, 4, 3, 35},
		{2, 4, 4, 3, 30},
		{2, 4, 4, 2, 40},
		{2, 4, 4, 2, 38},
		{2, 4, 4, 2, 33},
		{2, 4, 4, 2, 28},
		{2, 4, 4, 2, 23},
		{2, 4, 4, 2, 18},
		{2, 4, 4, 2, 13},
		{2, 4, 4, 2, 8},
		{2, 8, 8, 2, 8},
		{4, 4, 4, 2, 34},
		{4, 4, 4, 2, 29},
		{4, 4, 4, 2, 24},
		{4, 4, 4, 2, 20},
		{4, 4, 4, 2, 16},
		{4, 8, 8, 2, 20},
		{4, 8, 8, 2, 16},
		{4, 8, 8, 2, 12},
		{4, 8, 8, 2, 8},
		{4, 10, 12, 2, 8},
		{6, 12, 14, 2, 24},
		{6, 12, 14, 2, 20},
		{6, 12, 14, 2, 16},
		{6, 12, 14, 2, 12},
		{6, 12, 14, 2, 8},
		{8, 14, 16, 2, 16},
		{8, 14, 16, 2, 12},
		{8, 14, 16, 2, 8},
		{10, 16, 18, 2, 16},
		{10, 16, 18, 2, 12},
		{10, 16, 18, 2, 8},
		{10, 16, 18, 1, 8}
#else
		{2, 4, 4, 8, 48},
		{2, 4, 4, 8, 36},
		{2, 4, 4, 8, 24},
		{2, 4, 4, 6, 24},
		{2, 4, 4, 4, 24},
		{2, 4, 4, 4, 16},
		{2, 4, 4, 4, 8},
		{2, 4, 4, 2, 8},
		{2, 8, 8, 4, 16},
		{2, 8, 8, 4, 8},
		{2, 8, 8, 2, 8},
		{4, 4, 4, 8, 48},
		{4, 4, 4, 8, 40},
		{4, 4, 4, 8, 32},
		{4, 4, 4, 8, 24},
		{4, 4, 4, 6, 24},
		{4, 4, 4, 6, 16},
		{4, 4, 4, 4, 16},
		{4, 4, 4, 4, 8},
		{4, 4, 4, 2, 8},
		{4, 8, 8, 2, 8},
		{4, 10, 12, 2, 8},
		{6, 12, 14, 8, 32},
		{6, 12, 14, 8, 24},
		{6, 12, 14, 8, 16},
		{6, 12, 14, 6, 24},
		{6, 12, 14, 6, 16},
		{6, 12, 14, 4, 16},
		{6, 12, 14, 4, 8},
		{6, 12, 14, 2, 8},
		{8, 14, 16, 4, 16},
		{8, 14, 16, 4, 8},
		{8, 14, 16, 2, 8},
		{10, 16, 18, 4, 16},
		{10, 16, 18, 4, 8},
		{10, 16, 18, 2, 8}
#endif
	};
	CompareResult result[100];
	int rating_diff[1000];

	Com_SetOpening(com, 0);
	Com_LoadMPCInfo(com, MPC_LEARN_FILE);
	Com_SetOpening(com2, 0);
	Com_LoadMPCInfo(com2, MPC_LEARN_FILE);
	for (i = 0; i < 100; i++) {
		j = (int)array_num(info);
		result[i].win = 0;
		result[i].lose = 0;
		result[i].draw= 0;
//		result[i].rating = 1500 + 24 * (2 * i - j + 1) / 2;
		result[i].rating = 1500;
		result[i].rating_sum = 0;
	}
	for (i = 0; i < 1000; i++) {
		double diff = (double)1.0 / (1.0 + pow(10.0, (double)i/ 400)) * 16;
		if (diff < 0.5) {
			rating_diff[i] = 1;
		} else if (diff > 14.5) {
			rating_diff[i] = 16;
		} else {
			rating_diff[i] = (int)(diff + 0.5);
		}
	}
	for (round = 0; round < round_max; round++) {
		for (i = 0; i < array_num(info); i++) {
			result[i].rating_diff = 0;
			result[i].rating_count = 0;
		}
		for (i = 0; i < array_num(info); i++) {
			for (j = 0; j < array_num(info); j++) {
#if 0
				if (i == j || i < j - 5 || i > j + 5) {
#else
				if (i == j) {
#endif
					continue;
				}
				printf("%d / %d  %d / %d  %d / %d\r", round, round_max, i, array_num(info), j, array_num(info));
				Com_SetLevel(com, info[i].middle, info[i].exact, info[i].wld);
				Com_SetRandom(com, info[i].random, info[i].ratio);
				if (i >= 5) {
					Com_SetOpening(com, 1);
				} else {
					Com_SetOpening(com, 0);
				}
				Com_SetLevel(com2, info[j].middle, info[j].exact, info[j].wld);
				Com_SetRandom(com2, info[j].random, info[j].ratio);
				if (j >= 5) {
					Com_SetOpening(com2, 1);
				} else {
					Com_SetOpening(com2, 0);
				}
				Board_Clear(board);
				while (1) {
					moved = 0;
					if (Board_CanPlay(board, BLACK)) {
						move = Com_NextMove(com, board, BLACK, &value);
						Board_Flip(board, BLACK, move);
						moved = 1;
					}
					if (Board_CanPlay(board, WHITE)) {
						move = Com_NextMove(com2, board, WHITE, &value);
						Board_Flip(board, WHITE, move);
						moved = 1;
					}
					if (!moved) {
						break;
					}
				}
				count = Board_CountDisks(board, BLACK) - Board_CountDisks(board, WHITE);
				d = result[i].rating - result[j].rating;
				if (d >= 1000) {
					d = 1;
				} else if (d >= 0) {
					d = rating_diff[d];
				} else if (d <= -1000) {
					d = 15;
				} else {
					d = 16 - rating_diff[-d];
				}
				if (count > 0) {
					result[i].win++;
					result[j].lose++;
					result[i].rating_diff += d;
					result[i].rating_count++;
					result[j].rating_diff -= d;
					result[j].rating_count++;
				} else if (count < 0) {
					result[i].lose++;
					result[j].win++;
					result[i].rating_diff += (d - 16);
					result[i].rating_count++;
					result[j].rating_diff -= (d - 16);
					result[j].rating_count++;
				} else {
					result[i].draw++;
					result[j].draw++;
					result[i].rating_diff += (d - 8);
					result[i].rating_count++;
					result[j].rating_diff -= (d - 8);
					result[j].rating_count++;
				}
			}
		}
		printf("Round %d\n", round + 1);
		printf("Level Ratio Win Lose Draw\n");
		for (i = 0; i < array_num(info); i++) {
			result[i].rating += result[i].rating_diff;
			result[i].rating_diff = 0;
			result[i].rating_count = 0;
			if (round >= 2) {
				result[i].rating_sum += result[i].rating;
			}
			if (round <= 1) {
				result[i].average = result[i].rating;
			} else {
				result[i].average = (int)((double)result[i].average * 0.9 + (double)result[i].rating * 0.1 + 0.5);
			}
			printf("%d %.3f %d %d %d %d %d %d: {%d, %d, %d, %d, %d}\n", i,
				((double)(result[i].win * 2 + result[i].draw)) / (result[i].win + result[i].lose + result[i].draw) * 0.5,
				result[i].win, result[i].lose, result[i].draw, result[i].rating,
				(round > 1)?(result[i].rating_sum / (round - 1)):result[i].rating, result[i].average,
				info[i].middle, info[i].exact, info[i].wld, info[i].random, info[i].ratio);
		}
	}
	printf("終了しました                    \n");
}

static void compare_evaluator(Board *board, Com *com, Com *com2)
{
	int i, round;
	int move, value;
	int moved;
	int count;
	LevelInfo info[] = {
		{2, 8, 8, 2, 8},
		{4, 10, 12, 2, 8},
		{6, 12, 14, 2, 8},
		{8, 14, 16, 2, 8},
//		{10, 16, 18, 2, 8}
	};
	CompareResult result[100];

	Com_SetOpening(com, 0);
	Com_LoadMPCInfo(com, MPC_LEARN_FILE);
	Com_SetOpening(com2, 0);
	Com_LoadMPCInfo(com2, MPC_LEARN_FILE);
	for (i = 0; i < 100; i++) {
		result[i].win = 0;
		result[i].lose = 0;
		result[i].draw= 0;
	}
	for (round = 0; round < 20; round++) {
		for (i = 0; i < array_num(info); i++) {
			printf("%d / %d  %d / %d      \r", round, 20, i, array_num(info));
			Com_SetLevel(com, info[i].middle, info[i].exact, info[i].wld);
			Com_SetRandom(com, info[i].random, info[i].ratio);
			Com_SetLevel(com2, info[i].middle, info[i].exact, info[i].wld);
			Com_SetRandom(com2, info[i].random, info[i].ratio);
			Board_Clear(board);
			while (1) {
				moved = 0;
				if (Board_CanPlay(board, BLACK)) {
					move = Com_NextMove(com, board, BLACK, &value);
					Board_Flip(board, BLACK, move);
					moved = 1;
				}
				if (Board_CanPlay(board, WHITE)) {
					move = Com_NextMove(com2, board, WHITE, &value);
					Board_Flip(board, WHITE, move);
					moved = 1;
				}
				if (!moved) {
					break;
				}
			}
			count = Board_CountDisks(board, BLACK) - Board_CountDisks(board, WHITE);
			if (count > 0) {
				result[i].win++;
			} else if (count < 0) {
				result[i].lose++;
			} else {
				result[i].draw++;
			}
		}
		for (i = 0; i < array_num(info); i++) {
			printf("%d / %d  %d / %d      \r", round, 20, i, array_num(info));
			Com_SetLevel(com, info[i].middle, info[i].exact, info[i].wld);
			Com_SetRandom(com, info[i].random, info[i].ratio);
			Com_SetLevel(com2, info[i].middle, info[i].exact, info[i].wld);
			Com_SetRandom(com2, info[i].random, info[i].ratio);
			Board_Clear(board);
			while (1) {
				moved = 0;
				if (Board_CanPlay(board, BLACK)) {
					move = Com_NextMove(com2, board, BLACK, &value);
					Board_Flip(board, BLACK, move);
					moved = 1;
				}
				if (Board_CanPlay(board, WHITE)) {
					move = Com_NextMove(com, board, WHITE, &value);
					Board_Flip(board, WHITE, move);
					moved = 1;
				}
				if (!moved) {
					break;
				}
			}
			count = Board_CountDisks(board, BLACK) - Board_CountDisks(board, WHITE);
			if (count < 0) {
				result[i].win++;
			} else if (count > 0) {
				result[i].lose++;
			} else {
				result[i].draw++;
			}
		}
	}
	printf("Level Ratio Win Lose Draw\n");
	for (i = 0; i < array_num(info); i++) {
		printf("%d %.3f %d %d %d : {%d, %d, %d, %d, %d}\n", i,
			((double)(result[i].win * 2 + result[i].draw)) / (result[i].win + result[i].lose + result[i].draw) * 0.5,
			result[i].win, result[i].lose, result[i].draw,
			info[i].middle, info[i].exact, info[i].wld, info[i].random, info[i].ratio);
	}
	printf("終了しました                    \n");
}

static void learn2(Board *board, Evaluator *evaluator, char *in_file_name)
{
	FILE *fp;
	GameInfo info;
	int history_color[BOARD_SIZE * BOARD_SIZE];
	int color;
	int turn;
	int result;
	int pos;
	const int repeat = 50;
//	const int lambda = 977;
	const int lambda = 1000;
	int i, j;

	for (i = 0; i < repeat; i++) {
		printf("学習中... %d / %d \r", i, repeat);
		fp = fopen(in_file_name, "rb");
		while (fread(&info, sizeof(info), 1, fp) == 1) {
			Board_Clear(board);
			color = BLACK;
			turn = 0;
			while (1) {
				if (Board_CanPlay(board, color)) {
					pos = info.Moves[turn];
					Board_Flip(board, color, pos);
					history_color[turn] = color;
					turn++;
				} else if (!Board_CanPlay(board, Board_OpponentColor(color))) {
					break;
				}
				color = Board_OpponentColor(color);
			}
			result = (Board_CountDisks(board, BLACK) - Board_CountDisks(board, WHITE)) * DISK_VALUE;
			for (j = Board_CountDisks(board, EMPTY); j < 8; j++) {
				turn--;
				Board_Unflip(board);
			}
			for (j = Board_CountDisks(board, EMPTY); j < BOARD_SIZE * BOARD_SIZE - 12; j++) {
				turn--;
				Board_Unflip(board);
				Evaluator_SetStage(evaluator, Board_CountDisks(board, EMPTY));
				result = (result * lambda + info.Values[turn] * (1000 - lambda)) / 1000;
				if (history_color[turn] == BLACK) {
					Evaluator_Add(evaluator, board, result);
				} else if (history_color[turn] == WHITE) {
					Board_Reverse(board);
					Evaluator_Add(evaluator, board, -result);
					Board_Reverse(board);
				}
			}
		}
		fclose(fp);
		Evaluator_Update(evaluator);
		Evaluator_Save(evaluator, EVALUATOR_FILE);
	}
	printf("終了しました                    \n");
}

typedef struct _GameNode GameNode;
struct _GameNode {
	HashValue hash;
	GameNode *child;
	GameNode *next;
	int move;
	int value;
};

static GameNode *search_child_node(const GameNode *in_node, const HashValue *in_hash)
{
	GameNode *node = in_node->child;
	while (node != NULL) {
		if (node->hash.Low == in_hash->Low && node->hash.High == in_hash->High) {
			return node;
		}
		node = node->next;
	}
	return NULL;
}

static void add_child_node(GameNode *parent, GameNode *child)
{
	child->next = parent->child;
	parent->child = child;
}

static void update_node(GameNode *node, int color)
{
	GameNode *child = node->child;
	int value = node->value;
	if (child != NULL) {
		value = child->value;
	}
	if (color == BLACK) {
		while (child != NULL) {
			if (child->value > value) {
				value = child->value;
			}
			child = child->next;
		}
	} else {
		while (child != NULL) {
			if (child->value < value) {
				value = child->value;
			}
			child = child->next;
		}
	}
	node->value = value;
}

static void evaluate_node(GameNode *node, Board *board, int color, Evaluator *evaluator)
{
	GameNode *child = node->child;

	while (child != NULL) {
		Board_Flip(board, color, child->move);
		if (Board_CanPlay(board, Board_OpponentColor(color))) {
			evaluate_node(child, board, Board_OpponentColor(color), evaluator);
		} else {
			evaluate_node(child, board, color, evaluator);
		}
		Board_Unflip(board);
		child = child->next;
	}
	Evaluator_SetStage(evaluator, Board_CountDisks(board, EMPTY));
	if (color == BLACK) {
		Evaluator_Add(evaluator, board, node->value);
	} else if (color == WHITE) {
		Board_Reverse(board);
		Evaluator_Add(evaluator, board, -node->value);
		Board_Reverse(board);
	}
}

static void learn3(Board *board, Evaluator *evaluator, char *in_file_name)
{
	FILE *fp;
	GameNode *root_node;
	GameNode *free_node;
	GameNode *current_node;
	GameNode *node;
	HashValue hash;

	GameInfo info;
	int history_color[BOARD_SIZE * BOARD_SIZE];
	GameNode *history_node[BOARD_SIZE * BOARD_SIZE];
	int color;
	int turn;
	int result;
	int pos;
	const int repeat = 50;
	int i;

	root_node = calloc(sizeof(GameNode), 60 * 300000);
	if (!root_node) {
		printf("メモリが足りません\n");
		return;
	}
	free_node = root_node;
	node = free_node;
	free_node++;
	node->hash.Low = 0;
	node->hash.High = 0;
	node->child = NULL;
	node->next = NULL;
	node->value = 0;

	fp = fopen(in_file_name, "rb");
	while (fread(&info, sizeof(info), 1, fp) == 1) {
		Board_Clear(board);
		color = BLACK;
		turn = 0;
		pos = NOMOVE;
		current_node = root_node;
		while (1) {
			if (Board_CanPlay(board, color)) {
				if (turn < BOARD_SIZE * BOARD_SIZE - 12) {
					Board_HashValue(board, color, &hash);
					node = search_child_node(current_node, &hash);
					if (node == NULL) {
						node = free_node;
						free_node++;
						node->hash = hash;
						node->child = NULL;
						node->next = NULL;
						node->move = pos;
						node->value = 0;
						add_child_node(current_node, node);
					}
					history_node[turn] = node;
					current_node = node;
				} else {
					history_node[turn] = NULL;
				}
				pos = info.Moves[turn];
				Board_Flip(board, color, pos);
				history_color[turn] = color;
				turn++;
			} else if (!Board_CanPlay(board, Board_OpponentColor(color))) {
				break;
			}
			color = Board_OpponentColor(color);
		}
		result = (Board_CountDisks(board, BLACK) - Board_CountDisks(board, WHITE)) * DISK_VALUE;
		do {
			turn--;
		} while (history_node[turn] == NULL);
		history_node[turn]->value = result;
		while (turn > 0) {
			update_node(history_node[turn], history_color[turn]);
			turn--;
		}
	}
	fclose(fp);
	printf("ノード数 %d", free_node - root_node);

	for (i = 0; i < repeat; i++) {
		printf("学習中... %d / %d \r", i, repeat);
		Board_Clear(board);
		evaluate_node(root_node->child, board, BLACK, evaluator);
		Evaluator_Update(evaluator);
		Evaluator_Save(evaluator, EVALUATOR_FILE);
	}
	free(root_node);
	printf("終了しました                    \n");
}

static void learn_beginning(Board *board, Evaluator *evaluator, char *in_file_name)
{
	FILE *fp;
	GameInfo info;
	int history_color[BOARD_SIZE * BOARD_SIZE];
	int color;
	int turn;
	int result;
	int pos;
	const int repeat = 50;
//	const int lambda = 977;
	const int lambda = 1000;
	int i, j;

	for (i = 0; i < repeat; i++) {
		printf("学習中... %d / %d \r", i, repeat);
		fp = fopen(in_file_name, "rb");
		while (fread(&info, sizeof(info), 1, fp) == 1) {
			Board_Clear(board);
			color = BLACK;
			turn = 0;
			while (1) {
				if (Board_CanPlay(board, color)) {
					pos = info.Moves[turn];
					Board_Flip(board, color, pos);
					history_color[turn] = color;
					turn++;
				} else if (!Board_CanPlay(board, Board_OpponentColor(color))) {
					break;
				}
				color = Board_OpponentColor(color);
			}
			result = (Board_CountDisks(board, BLACK) - Board_CountDisks(board, WHITE)) * DISK_VALUE;
			for (j = Board_CountDisks(board, EMPTY); j < 30; j++) {
				turn--;
				Board_Unflip(board);
			}
			for (j = Board_CountDisks(board, EMPTY); j < BOARD_SIZE * BOARD_SIZE - 10; j++) {
				turn--;
				Board_Unflip(board);
//				Evaluator_SetStage(evaluator, Board_CountDisks(board, EMPTY));
				Evaluator_SetStage(evaluator, BOARD_SIZE * BOARD_SIZE - 4);
				result = (result * lambda + info.Values[turn] * (1000 - lambda)) / 1000;
				if (history_color[turn] == BLACK) {
					Evaluator_Add(evaluator, board, result);
				} else if (history_color[turn] == WHITE) {
					Board_Reverse(board);
					Evaluator_Add(evaluator, board, -result);
					Board_Reverse(board);
				}
			}
		}
		fclose(fp);
		Evaluator_Update(evaluator);
		Evaluator_Save(evaluator, EVALUATOR_FILE);
	}
	printf("終了しました                    \n");
}

static void show_opening(FILE *fp, int turn, Board *board, int color, Opening *opening)
{
	PositionInfo info;
	int value;
	int i, j;
	char buffer[256];

	for (i = A1; i <= H8; i++) {
		if (Board_Flip(board, color, i)) {
			if (Opening_Info(opening, board, Board_OpponentColor(color), &info)) {
				value = PositionInfo_Value(&info);
				for (j = 0; j < turn * 2 - 2; j++) {
					buffer[j] = ' ';
				}
				buffer[j] = '+';
				j++;
				buffer[j] = '-';
				j++;
				if (color == BLACK) {
					buffer[j] = "ABCDEFGH"[Board_X(i)];
				} else {
					buffer[j] = "abcdefgh"[Board_X(i)];
				}
				j++;
				buffer[j] = "12345678"[Board_Y(i)];
				j++;
				buffer[j] = '\0';
				fprintf(fp, "%s %c%.2f\n", buffer, color==BLACK?'w':'b', (double)value / DISK_VALUE);
				show_opening(fp, turn + 1, board, Board_OpponentColor(color), opening);
			}
			Board_Unflip(board);
		}
	}
}

static void opening_initialize(Board *board, Opening *opening)
{
	FILE *fp;
	char buffer[TRANSCRIPT_SIZE], value_buffer[BUFFER_SIZE];
	PositionInfo info;
	int color, turn, value, max;
	int history_move[BOARD_SIZE * BOARD_SIZE * 2];
	int i;

	fp = fopen(OPENING_TRANSCRIPT_FILE, "r");
	if (!fp) {
		return;
	}
	while (1) {
		Board_Clear(board);
		color = BLACK;
		turn = 0;
		if (!get_stream(buffer, TRANSCRIPT_SIZE, fp)) {
			break;
		}
		if (!get_stream(value_buffer, BUFFER_SIZE, fp)) {
			break;
		}
		value = (int)(atof(value_buffer) * DISK_VALUE);
		for (i = 0; buffer[i] != '\0' && buffer[i + 1] != '\0'; i += 2) {
			if (!Board_CanPlay(board, color)) {
				history_move[turn] = PASS;
			} else {
				history_move[turn] = Board_Pos(tolower(buffer[i]) - 'a', buffer[i + 1] - '1');
				if (!Board_Flip(board, color, history_move[turn])) {
					break;
				}
			}
			turn++;
			color = Board_OpponentColor(color);
		}
		if (!Board_CanPlay(board, color)) {
			history_move[turn] = PASS;
			turn++;
			color = Board_OpponentColor(color);
		}
		history_move[turn] = NOMOVE;
		if (color == BLACK) {
			max = value;
		} else {
			max = -value;
		}
		for (; turn >= 0; turn--) {
			if (history_move[turn] == PASS) {
				if (Opening_Info(opening, board, Board_OpponentColor(color), &info)) {
					max = -PositionInfo_Value(&info);
				}
			} else {
				for (i = A1; i <= H8; i++) {
					if (Board_Flip(board, color, i)) {
						if (Opening_Info(opening, board, Board_OpponentColor(color), &info)) {
							if (-PositionInfo_Value(&info) > max) {
								max = -PositionInfo_Value(&info);
							}
						}
						Board_Unflip(board);
					}
				}
			}
			PositionInfo_SetValue(&info, max);
			Opening_SetInfo(opening, board, color, &info);
			Board_Unflip(board);
			color = Board_OpponentColor(color);
			max = -max;
		}
	}
	fclose(fp);
	Opening_Save(opening, OPENING_FILE);

	fp = fopen("opening_list.txt", "wt");
	fprintf(fp, "F5 0.00\n");
	Board_Clear(board);
	Board_Flip(board, BLACK, F5);
	show_opening(fp, 1, board, WHITE, opening);
	fclose(fp);

	printf("登録完了しました\n");
}

static int write_mpc(FILE *fp, int in_num, const MPCInfo *in_info)
{
	if (fwrite(&in_num, sizeof(int), 1, fp) < 1) {
		return 0;
	}
	if (fwrite(in_info, sizeof(MPCInfo), in_num, fp) < (size_t)in_num) {
		return 0;
	}
	return 1;
}

static int save_mpc(int in_array_num, int *in_num, const MPCInfo **in_info)
{
	FILE *fp;
	int i;

	fp = fopen(MPC_FILE, "wb");
	if (!fp) {
		return 0;
	}
	for (i = 0; i < in_array_num; i++) {
		if (!write_mpc(fp, in_num[i], in_info[i])) {
			fclose(fp);
			return 0;
		}
	}
	fclose(fp);
	return 1;
}

static void calc_mpc(Board *board, Com *com)
{
	MPCInfo info[STAGE_NUM][MPC_NUM];
	MPCInfo *tmp_info[STAGE_NUM];
	int info_num[STAGE_NUM];
	int depth[] = {1, 2, 1, 2, 3, 4, 3, 4, 5, 6, 5, 6, 5, 6};
	int stage, i, j, k;
	int num, value_low, value_high;
	int color;
	double mean, var;
	char transcript[][TRANSCRIPT_SIZE] = {
		"c4c3c2d6e6f6f5e3f4f3g5c6d3c5e2b4f7f2d2b3g4g6f1h5e7h3d7g3b5d8f8c8e8g8a4b6a7a5a6c7h4d1h6a3a2b7e1g1h2h7h8g7g2h1b8a8a1b1b2c1",
		"e6d6c6f4g3e7f6d7d8f5e8f3g4g5h4f8g8f7c7h3g6h5h6h7e3c5f2b6c3c4b4b5a4d3b3a5h2h1g2e1c2e2a6b8c8h8g7b7a8a7d1d2c1g1f1b2a3a1b1a2",
		"f5d6c3g5g6f3f4e3d3c4h5f6g3f2e6e7c6f7c5g4h4h3h2b5d2b4e2c1d1f1c2e1d7b3a5a6a4h7a7c8a3b2h6h1a1g2g1a2b1b6d8e8f8a8b7c7b8g7g8h8",
		"e6f4c3c4d3c2f3d6f5e3e2f1c5c6d7f2d2e1c1g3b1g4g5c7b4d1g1b3b6f6e7b5g6d8h5e8a6f7a5a3a4a7h4a2g7h3f8h6h7h8g8b7a8c8a1b2b8g2h2",
		"e6d6c5b4c6e7b5f6e8b6d7f4g6f5c4c7c8f7g5d8f8g4a3h5c3e3d3h6e2f2a4a5a6e1f1g1f3g3g7d1h3d2h4h8c2c1b3g8b1a1g2a2b2h2b7b8h1a7a8h7",
		"c4e3f5c6e2f3d3d2c3f4f2b4c5g4c1b6b3e6f6c2d6a3b5a4b1g3c7g5g6f7e7h5b2a1a5e8h3b8a6d7h4f1d1h2b7e1h7a7h6h8g7g2c8d8a8g8h1g1f8a2",
		"c4e3f2c3f3c5b4d3e2d2c1f4b3e1f1d1c2b5f5a5a4a3f6e6e7g3g4g2d6c6b6d7a6f8a2b2a1b1c8c7b8h4h5h6b7g6g5f7h1g1h3h2h7a8a7d8g8e8g7h8",
		"f5f4c3g6f3d3g4f2g3h4e6h5e3f6g5d6f1c5f7c4h6e7h3f8g7e2e1d2d1c2b3c1b4g1b6c6c7d7c8h8d8a5e8h7g8g2a4a6b7b5a7h2h1a3a2b2a1b1b8a8",
		"c4c5f6f3d6f5c6c3e6b5d3b4g4e3f4c7f2d7e2g5c2h3b6c1g6a5d8d2c8g3d1b7b1f7e8e7b3f8h6b8a8a7h5h4h2g7h7b2g2h1g1e1f1a1a4h8g8a3a6a2",
		"c4c3c2f4f6d6f3c5f5d3e3d2e6g4d7c6b4e7c7c8e2f2g3b5a6b3a3f1b6c1d8e8f8g8f7h4e1d1g2g6h5h6h3g5g1b8b1h1h2a1b2a2b7a8a7a5a4g7h8h7",
		"f5f4f3g4h3f6g5e6e3g3c4f2g6e2f1c6h4c5e7c3d6d7c8f7c7d8e8g8f8b8b3b4e1g7d3d2a4g1h1b5a5h2b6h7g2a6d1c2h8b2a2a3a1h5a8b1h6b7a7c1",
		"d3e3f2c5d6c2f3e2c3c6f5e6f6d2c4f4f1e1c1g1g5g6d1b1e7g3g4b5b3b4h3a3d7h6h5c7b6d8a5a4a6a7g2f8f7h1e8h2h4g7g8h8h7b7c8b8a8a2b2a1",
		"c4c3e6f6f5d6c5e3d3c6f2d2c2c1d1f3f4e1e7g4g3e2h3g6g5f1h6f8d8g7f7e8d7c8c7g8h8b6b5a6a5b7b3a4b8b4a8a7b2h4a3a1h5h7g1h1h2a2b1g2",
		"c4c3c2f4f5b2e3c5d3e2b3f2g3f6f3d6b6g4c6e6h4d2g5g6a1b4a4b5a5c7c1a3a2b1e1d1f1h5g2h3h2a7e7f7a6e8a8b7d8h1g1b8c8d7f8g7g8h8h7h6",
		"e6f4e3d2g3g5g4f6d6d7c5f5c4f3d3c3c1c6e7c2b5f2b6c7e2b4a3d1f1b1h6d8f8h4b3e8c8h2g6h7h8f7h3h5e1b2a1a2g2a5a6h1a4g1g8g7b7b8a7a8",
		"f5f4c3c6e3f6g3f3g4d3g5e6d6g6c5c4f7h4h3h5d7c7b4b5b3c2e7f2h6a5a3d8c8b8a4a2e2e1d1c1d2g2h1h2g1g8f8a6b7e8b6g7h8a8a7h7a1b2b1f1",
		""
	};

	for (stage = 0; stage < STAGE_NUM; stage++) {
		info_num[stage] = 0;
		tmp_info[stage] = info[stage];
	}
	save_mpc(STAGE_NUM, info_num, tmp_info);
	Com_LoadMPCInfo(com, MPC_FILE);
	Com_SetRandom(com, 0, 0);
	for (stage = 0; stage < STAGE_NUM; stage++) {
		for (i = 0; i < MPC_NUM; i++) {
			num = 0;
			mean = var = 0.0;
			printf("MPC計算中 %d / %d  %d / %d          \r", stage, STAGE_NUM, i, MPC_NUM);
			for (j = 0; transcript[j][0] != '\0';j++) {
				Board_Clear(board);
				color = BLACK;
				for (k = 0; transcript[j][k] != '\0' && transcript[j][k+1] != '\0'; k += 2) {
					if (!Board_Flip(board, color, Board_Pos(tolower(transcript[j][k]) - 'a', transcript[j][k+1] - '1'))) {
						break;
					}
					if (Board_CanPlay(board, color)) {
						color = Board_OpponentColor(color);
					}
					if (k < 4 || Evaluator_GetStageIndex(k + i + MPC_DEPTH_MIN) != stage) {
						continue;
					} else if (Board_CountDisks(board, EMPTY) <= i + MPC_NUM + 6) {
						break;
					}
					Com_SetLevel(com, depth[i], 0, 0);
					Com_NextMove(com, board, color, &value_low);
					Com_SetLevel(com, i + MPC_DEPTH_MIN, 0, 0);
					Com_NextMove(com, board, color, &value_high);
					num++;
					mean += (double)(value_high - value_low);
					var += (double)(value_high - value_low) * (double)(value_high - value_low);
				}
			}
			mean /= num;
			var /= num;
			info[stage][i].Depth = depth[i];
			info[stage][i].Offset = (int)mean;
			info[stage][i].Deviation = (int)(2.0 * sqrt(var - mean * mean));
			info_num[stage] = i + 1;
			save_mpc(STAGE_NUM, info_num, tmp_info);
			Com_LoadMPCInfo(com, MPC_FILE);
		}
	}
	printf("計算完了しました                    \n");
}

static int main_param_initialize_each(MainParam *self)
{
	self->Board = Board_New();
	if (!self->Board) {
		return 0;
	}
	self->Evaluator = Evaluator_New();
	if (!self->Evaluator) {
		return 0;
	}
	Evaluator_Load(self->Evaluator, EVALUATOR_FILE);
	self->Evaluator2 = Evaluator_New();
	if (!self->Evaluator2) {
		return 0;
	}
	Evaluator_Load(self->Evaluator2, EVALUATOR_FILE_2);
	self->Opening = Opening_New();
	if (!self->Opening) {
		return 0;
	}
	Opening_Load(self->Opening, OPENING_FILE);
	self->Com = Com_New(self->Evaluator, self->Opening);
	if (!self->Com) {
		return 0;
	}
	self->Com2 = Com_New(self->Evaluator, self->Opening);
	if (!self->Com2) {
		return 0;
	}
	self->Com3 = Com_New(self->Evaluator, self->Opening);
	if (!self->Com3) {
		return 0;
	}

	return 1;
}

static int main_param_initialize(MainParam *self)
{
	memset(self, 0, sizeof(MainParam));
	if (!main_param_initialize_each(self)) {
		main_param_finalize(self);
		return 0;
	}

	return 1;
}

static void main_param_finalize(MainParam *self)
{
	if (self->Com3) {
		Com_Delete(self->Com3);
	}
	if (self->Com2) {
		Com_Delete(self->Com2);
	}
	if (self->Com) {
		Com_Delete(self->Com);
	}
	if (self->Opening) {
		Opening_Delete(self->Opening);
	}
	if (self->Evaluator2) {
		Evaluator_Delete(self->Evaluator2);
	}
	if (self->Evaluator) {
		Evaluator_Delete(self->Evaluator);
	}
	if (self->Board) {
		Board_Delete(self->Board);
	}
}

int main(int argc, char **argv)
{
	MainParam param;
	char buffer[BUFFER_SIZE];

	srand((unsigned)time(NULL));
	if (!main_param_initialize(&param)) {
		printf("初期化に失敗しました\n");
		return 0;
	}

	while (1) {
		printf("モードを選択してください (1:対戦 2:学習 3:定石登録 4:MPC計算 5:自己対局 6:学習2\n");
		printf("7:初期盤面学習 8:対局テスト 9:学習3 a:評価関数比較 q:終了)\n");
		get_stream(buffer, BUFFER_SIZE, stdin);
		if (!strcmp(buffer, "1")) {
			play(param.Board, param.Com);
		} else if (!strcmp(buffer, "2")) {
			learn(param.Board, param.Evaluator, param.Com);
		} else if (!strcmp(buffer, "3")) {
			opening_initialize(param.Board, param.Opening);
		} else if (!strcmp(buffer, "4")) {
			calc_mpc(param.Board, param.Com);
		} else if (!strcmp(buffer, "5")) {
			self_play(param.Board, param.Evaluator, param.Com, GAME_INFO_FILE);
		} else if (!strcmp(buffer, "6")) {
			learn2(param.Board, param.Evaluator, GAME_INFO_FILE);
		} else if (!strcmp(buffer, "7")) {
			learn_beginning(param.Board, param.Evaluator, GAME_INFO_FILE);
		} else if (!strcmp(buffer, "8")) {
			compare_com(param.Board, param.Evaluator, param.Com, param.Com2);
		} else if (!strcmp(buffer, "9")) {
			learn3(param.Board, param.Evaluator, GAME_INFO_FILE);
		} else if (!strcmp(buffer, "a")) {
			compare_evaluator(param.Board, param.Com, param.Com3);
		} else if (!strcmp(buffer, "0")) {
			self_play(param.Board, param.Evaluator, param.Com, GAME_INFO_FILE);
			learn2(param.Board, param.Evaluator, GAME_INFO_FILE);
			self_play(param.Board, param.Evaluator, param.Com, GAME_INFO_FILE);
			learn2(param.Board, param.Evaluator, GAME_INFO_FILE);
			self_play(param.Board, param.Evaluator, param.Com, GAME_INFO_FILE);
			learn2(param.Board, param.Evaluator, GAME_INFO_FILE);
		} else if (!strcmp(buffer, "q")) {
			break;
		}
	}

	main_param_finalize(&param);

	return 0;
}
