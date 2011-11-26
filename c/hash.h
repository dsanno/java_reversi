#ifndef HASH_H
#define HASH_H

/* ƒnƒbƒVƒ…’l */
struct _HashValue
{
	unsigned long Low;		/* ‰º32bit */
	unsigned long High;		/* ã32bit */
};
typedef struct _HashValue HashValue;

/* ‹Ç–Êî•ñ */
struct _HashInfo
{
	int Lower;				/* •]‰¿’l‚Ì‰ºŒÀ */
	int Upper;				/* •]‰¿’l‚ÌãŒÀ */
	unsigned char Depth;	/* ’Tõè” */
	unsigned char Move;		/* Å‘Pè */
};
typedef struct _HashInfo HashInfo;

typedef struct _Hash Hash;

#ifdef __cplusplus
extern "C" {
#endif
Hash	*Hash_New(int in_size);
void	Hash_Delete(Hash *self);
void	Hash_Clear(Hash *self);
int		Hash_Set(Hash *self, const HashValue *in_value, const HashInfo *in_info);
int		Hash_Get(Hash *self, const HashValue *in_value, HashInfo *out_info);
void	Hash_ClearInfo(Hash *self);
int		Hash_CountGet(Hash *self);
int		Hash_CountHit(Hash *self);
#ifdef __cplusplus
}
#endif

#endif /* HASH_H */
