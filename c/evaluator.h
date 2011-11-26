#ifndef EVALUATOR_H
#define EVALUATOR_H

#include "board.h"

/* ÇPêŒÇ†ÇΩÇËÇÃï]âøíl */
#define DISK_VALUE 100

/* ï]âøä÷êîÇÃíiäKêî */
#define STAGE_NUM 3

typedef struct _Evaluator Evaluator;

#ifdef __cplusplus
extern "C" {
#endif
Evaluator	*Evaluator_New(void);
void		Evaluator_Delete(Evaluator *self);

int			Evaluator_Load(Evaluator *self, const char *in_file_name);
int			Evaluator_Save(const Evaluator *self, const char *in_file_name);

int			Evaluator_Value(const Evaluator *self, const Board *in_board);
void		Evaluator_SetStage(Evaluator *self, int in_empty_count);
int			Evaluator_GetStageIndex(int in_empty_count);
void		Evaluator_Add(Evaluator *self, const Board *in_board, int in_value);
void		Evaluator_Update(Evaluator *self);
#ifdef __cplusplus
}
#endif

#endif /* EVALUATOR_H */
