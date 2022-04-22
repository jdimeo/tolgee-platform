import { styled } from '@mui/material';

export const TABLE_TOP_ROW = 'topRow';
export const TABLE_FIRST_CELL = 'firstCell';
export const TABLE_LAST_CELL = 'lastCell';
export const TABLE_CENTERED = 'centered';
export const TABLE_DIVIDER = 'divider';

export const StyledLanguageTable = styled('div')`
  display: grid;
  align-items: center;
  border: 1px ${({ theme }) => theme.palette.extraLightDivider.main} solid;
  border-radius: 4px;
  overflow: hidden;
  position: relative;

  & .${TABLE_TOP_ROW} {
    display: flex;
    background: ${({ theme }) => theme.palette.extraLightBackground.main};
    align-self: stretch;
    font-size: 13px;
    min-width: 60px;
    height: 24px;
    padding: ${({ theme }) => theme.spacing(0, 1)};
    align-items: center;
  }

  & .${TABLE_FIRST_CELL} {
    padding-left: ${({ theme }) => theme.spacing(2)};
  }

  & .${TABLE_LAST_CELL} {
    justify-self: end;
    padding-right: ${({ theme }) => theme.spacing(2)};
  }

  & .${TABLE_CENTERED} {
    display: flex;
    justify-self: stretch;
    justify-content: center;
  }

  & .${TABLE_DIVIDER} {
    grid-column: 1 / -1;
    background: ${({ theme }) => theme.palette.lightBackground.main};
    height: 1px;
  }
`;