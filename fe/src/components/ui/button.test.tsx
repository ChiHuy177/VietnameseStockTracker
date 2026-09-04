import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { Button } from './button'

describe('Button', () => {
  it('renders its label', () => {
    render(<Button>Search</Button>)

    expect(screen.getByRole('button', { name: 'Search' })).toBeInTheDocument()
  })

  it('calls onClick when clicked', async () => {
    const onClick = vi.fn()
    render(<Button onClick={onClick}>Search</Button>)

    await userEvent.click(screen.getByRole('button', { name: 'Search' }))

    expect(onClick).toHaveBeenCalledOnce()
  })

  it('is disabled when the disabled prop is set', () => {
    render(<Button disabled>Search</Button>)

    expect(screen.getByRole('button', { name: 'Search' })).toBeDisabled()
  })
})
