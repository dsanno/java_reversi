#ifndef HASH_H
#define HASH_H

/* �n�b�V���l */
struct _HashValue
{
	unsigned long Low;		/* ��32bit */
	unsigned long High;		/* ��32bit */
};
typedef struct _HashValue HashValue;

/* �ǖʏ�� */
struct _HashInfo
{
	int Lower;				/* �]���l�̉��� */
	int Upper;				/* �]���l�̏�� */
	unsigned char Depth;	/* �T���萔 */
	unsigned char Move;		/* �őP�� */
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
